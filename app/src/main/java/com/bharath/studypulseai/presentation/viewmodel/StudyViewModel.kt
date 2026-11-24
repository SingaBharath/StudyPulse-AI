package com.bharath.studypulseai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bharath.studypulseai.data.repository.QuizQuestion
import com.bharath.studypulseai.data.repository.StudyRepository
import com.bharath.studypulseai.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── UI State types ─────────────────────────────────────────────────────────────

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data object Empty   : NotesUiState
    data class  Success(val notes: List<StudyNote>) : NotesUiState
    data class  Error(val message: String)          : NotesUiState
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEvent
    data class NavigateToDetail(val noteId: Int) : UiEvent
}

// ── ViewModel ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    // ── UI input state ────────────────────────────────────────────────────────
    private val _searchQuery  = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(NoteFilter.ALL)
    private val _sortOrder    = MutableStateFlow(SortOrder.NEWEST)

    val searchQuery:  StateFlow<String>     = _searchQuery.asStateFlow()
    val activeFilter: StateFlow<NoteFilter> = _activeFilter.asStateFlow()
    val sortOrder:    StateFlow<SortOrder>  = _sortOrder.asStateFlow()

    // ── Async flags ───────────────────────────────────────────────────────────
    private val _isGeneratingAi   = MutableStateFlow(false)
    private val _isGeneratingQuiz  = MutableStateFlow(false)
    private val _regeneratingNoteId = MutableStateFlow<Int?>(null)

    val isGeneratingAi:    StateFlow<Boolean> = _isGeneratingAi.asStateFlow()
    val isGeneratingQuiz:  StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()
    val regeneratingNoteId: StateFlow<Int?>    = _regeneratingNoteId.asStateFlow()

    private var regenerationCounter = 0

    // ── Quiz state ────────────────────────────────────────────────────────────
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    // ── One-shot events ───────────────────────────────────────────────────────
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    // ── Stats ─────────────────────────────────────────────────────────────────
    val stats: StateFlow<StudyStats> = combine(
        repository.totalCount,
        repository.completedCount,
        repository.totalReviews,
        repository.dueCount(),
    ) { total, completed, reviews, due ->
        StudyStats(total = total, completed = completed, totalReviews = reviews, dueForReview = due)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudyStats())

    // ── Notes list (reactive + filtered + searched + sorted) ──────────────────
    val notesUiState: StateFlow<NotesUiState> = combine(
        _activeFilter,
        _searchQuery.debounce(280),
        _sortOrder,
    ) { filter, query, sort -> Triple(filter, query, sort) }
        .flatMapLatest { (filter, query, sort) ->
            val base: Flow<List<StudyNote>> = when {
                query.isNotBlank()                   -> repository.search(query)
                filter == NoteFilter.PENDING         -> repository.pendingNotes
                filter == NoteFilter.COMPLETED       -> repository.completedNotes
                filter == NoteFilter.PINNED          -> repository.pinnedNotes
                filter == NoteFilter.DUE             -> repository.dueNotes()
                else                                 -> repository.allNotes
            }
            base.map { list ->
                val sorted = when (sort) {
                    SortOrder.OLDEST       -> list.sortedBy { it.createdAt }
                    SortOrder.ALPHABETICAL -> list.sortedBy { it.subject.lowercase() }
                    SortOrder.DIFFICULTY   -> list.sortedByDescending { it.difficulty.ordinal }
                    SortOrder.REVIEW_COUNT -> list.sortedByDescending { it.reviewCount }
                    else                   -> list   // NEWEST — DB already sorts DESC
                }
                if (sorted.isEmpty()) NotesUiState.Empty
                else NotesUiState.Success(sorted)
            }
        }
        .catch { emit(NotesUiState.Error(it.message ?: "Unexpected error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState.Loading)

    // ── Public actions ────────────────────────────────────────────────────────

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun onFilterChange(f: NoteFilter) { _activeFilter.value = f }
    fun onSortChange(s: SortOrder)    { _sortOrder.value = s }

    fun addNoteWithAi(subject: String, topicNotes: String, difficulty: Difficulty) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            repository.generateAiSummary(subject, topicNotes)
                .onSuccess { summary ->
                    repository.insert(
                        StudyNote(
                            subject            = subject.trim(),
                            topicNotes         = topicNotes.trim(),
                            aiFlashcardSummary = summary,
                            difficulty         = difficulty,
                        )
                    )
                    sendEvent(UiEvent.ShowSnackbar("Flashcard created and saved"))
                }
                .onFailure { e ->
                    sendEvent(UiEvent.ShowSnackbar("Generation failed: ${e.message}", "Retry"))
                }
            _isGeneratingAi.value = false
        }
    }

    fun toggleCompletion(note: StudyNote) {
        viewModelScope.launch {
            repository.setCompleted(note.id, !note.isCompleted)
            val msg = if (!note.isCompleted) "Topic marked as mastered" else "Topic moved back to pending"
            sendEvent(UiEvent.ShowSnackbar(msg))
        }
    }

    fun togglePin(note: StudyNote) {
        viewModelScope.launch {
            repository.setPinned(note.id, !note.isPinned)
        }
    }

    fun deleteNote(note: StudyNote) {
        viewModelScope.launch {
            repository.delete(note)
            sendEvent(UiEvent.ShowSnackbar("\"${note.subject}\" removed"))
        }
    }

    fun markReviewed(note: StudyNote) {
        viewModelScope.launch {
            repository.recordReview(note.id)
            sendEvent(UiEvent.ShowSnackbar("Review logged — next session scheduled"))
        }
    }

    fun regenerateFlashcard(note: StudyNote) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            _regeneratingNoteId.value = note.id
            regenerationCounter++
            repository.generateAiSummary(note.subject, note.topicNotes, styleIndex = regenerationCounter)
                .onSuccess { newSummary ->
                    repository.update(note.copy(aiFlashcardSummary = newSummary))
                    sendEvent(UiEvent.ShowSnackbar("AI Flashcard refreshed with new perspective!"))
                }
                .onFailure {
                    sendEvent(UiEvent.ShowSnackbar("Regeneration failed"))
                }
            _regeneratingNoteId.value = null
            _isGeneratingAi.value = false
        }
    }

    fun generateQuiz(note: StudyNote) {
        viewModelScope.launch {
            _isGeneratingQuiz.value = true
            repository.generateQuizQuestions(note)
                .onSuccess { _quizQuestions.value = it }
                .onFailure { sendEvent(UiEvent.ShowSnackbar("Could not generate quiz")) }
            _isGeneratingQuiz.value = false
        }
    }

    fun clearQuiz() { _quizQuestions.value = emptyList() }

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val repository: StudyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StudyViewModel(repository) as T
    }
}
