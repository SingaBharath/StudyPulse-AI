package com.bharath.studypulseai.data.repository

import com.bharath.studypulseai.data.local.dao.StudyNoteDao
import com.bharath.studypulseai.data.mapper.StudyNoteMapper.toDomain
import com.bharath.studypulseai.data.mapper.StudyNoteMapper.toDomainList
import com.bharath.studypulseai.data.mapper.StudyNoteMapper.toEntity
import com.bharath.studypulseai.domain.model.Difficulty
import com.bharath.studypulseai.domain.model.StudyNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudyRepository(private val dao: StudyNoteDao) {

    // ── Observe ───────────────────────────────────────────────────────────────

    val allNotes: Flow<List<StudyNote>>
        get() = dao.observeAll().map { it.toDomainList() }

    val pendingNotes: Flow<List<StudyNote>>
        get() = dao.observePending().map { it.toDomainList() }

    val completedNotes: Flow<List<StudyNote>>
        get() = dao.observeCompleted().map { it.toDomainList() }

    val pinnedNotes: Flow<List<StudyNote>>
        get() = dao.observePinned().map { it.toDomainList() }

    fun dueNotes(now: Long = System.currentTimeMillis()): Flow<List<StudyNote>> =
        dao.observeDueForReview(now).map { it.toDomainList() }

    fun search(query: String): Flow<List<StudyNote>> =
        dao.search(query).map { it.toDomainList() }

    val totalCount: Flow<Int>     = dao.observeTotalCount()
    val completedCount: Flow<Int> = dao.observeCompletedCount()
    fun dueCount(now: Long = System.currentTimeMillis()): Flow<Int> = dao.observeDueCount(now)
    val totalReviews: Flow<Int>   = dao.observeTotalReviews().map { it ?: 0 }

    // ── Mutations ─────────────────────────────────────────────────────────────

    suspend fun insert(note: StudyNote): Long = dao.insert(note.toEntity())
    suspend fun update(note: StudyNote)       = dao.update(note.toEntity())
    suspend fun delete(note: StudyNote)       = dao.delete(note.toEntity())
    suspend fun setCompleted(id: Int, done: Boolean)  = dao.setCompleted(id, done)
    suspend fun setPinned(id: Int, pinned: Boolean)   = dao.setPinned(id, pinned)

    suspend fun recordReview(id: Int) {
        val entity = dao.findById(id) ?: return
        val next   = scheduleNextReview(entity.reviewCount)
        dao.recordReview(id, next)
    }

    // ── AI Flashcard Generation (Multi-Perspective & Dynamic) ─────────────────

    /**
     * Synthesizes a structured AI Flashcard from study notes.
     * Takes [styleIndex] to generate distinct, fresh perspectives on regeneration:
     *  0: Core Concepts & Foundational Definitions
     *  1: Exam High-Yield & Common Pitfalls
     *  2: Deep-Dive & Architectural Comparisons
     *  3: Rapid-Fire Flashcard Q&A (Front & Back)
     */
    suspend fun generateAiSummary(subject: String, rawNotes: String, styleIndex: Int = 0): Result<String> =
        runCatching {
            kotlinx.coroutines.delay(800) // Natural AI processing feel

            val trimmedNotes = rawNotes.trim()
            if (trimmedNotes.isBlank()) {
                return@runCatching "Review core concepts of $subject."
            }

            // Extract concepts without breaking on commas, acronyms, or numbers
            val rawLines = trimmedNotes
                .lines()
                .map { line ->
                    line.trim()
                        .trimStart('-', '*', '•', '–')
                        .replaceFirst(Regex("^\\d+([.)]|\\s)\\s*"), "")
                        .trim()
                }
                .filter { it.isNotBlank() }

            val candidatePoints = if (rawLines.size >= 2) {
                rawLines
            } else {
                trimmedNotes
                    .split(Regex("(?<=[.!?])\\s+"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            val extractedPoints = candidatePoints
                .filter { it.length >= 3 }
                .distinctBy { it.lowercase() }

            val words = trimmedNotes.split(Regex("\\s+")).size
            val estMins = (words / 12).coerceIn(10, 60)

            // Select key term if present
            val keyTerm = when {
                candidatePoints.isNotEmpty() && candidatePoints[0].contains(":") ->
                    candidatePoints[0].substringBefore(":").trim()
                candidatePoints.isNotEmpty() && candidatePoints[0].contains(" - ") ->
                    candidatePoints[0].substringBefore(" - ").trim()
                else -> subject
            }

            val mode = (styleIndex % 4 + 4) % 4
            when (mode) {
                0 -> {
                    // Perspective 1: Core Concepts & Foundational Definitions
                    val points = if (extractedPoints.isNotEmpty()) {
                        extractedPoints.take(4).mapIndexed { i, pt -> "${i + 1}. $pt" }.joinToString("\n")
                    } else {
                        "1. Fundamental theory of $subject\n2. Key structural rules\n3. Core applications"
                    }
                    buildString {
                        appendLine("Key Concepts & Definitions:")
                        appendLine(points)
                        appendLine()
                        appendLine("Active Recall Challenge:")
                        appendLine("Q: Define '$keyTerm' in your own words and explain how it operates.")
                        appendLine()
                        appendLine("Study Plan:")
                        appendLine("Session: $estMins mins | Focus: Active Recall & Definition Mastery")
                    }.trimEnd()
                }
                1 -> {
                    // Perspective 2: Exam Pitfalls & High-Yield Mechanics
                    val reversedPoints = extractedPoints.reversed().take(4)
                    val points = if (reversedPoints.isNotEmpty()) {
                        reversedPoints.mapIndexed { i, pt -> "• $pt" }.joinToString("\n")
                    } else {
                        "• Common edge cases in $subject\n• Critical exam pitfalls and trade-offs"
                    }
                    buildString {
                        appendLine("Exam High-Yield & Mechanics:")
                        appendLine(points)
                        appendLine()
                        appendLine("Active Recall Challenge:")
                        appendLine("Q: What is the most critical constraint or pitfall regarding $keyTerm?")
                        appendLine()
                        appendLine("Study Plan:")
                        appendLine("Session: $estMins mins | Technique: Feynman Technique (Explain to a beginner)")
                    }.trimEnd()
                }
                2 -> {
                    // Perspective 3: Architecture & Deep-Dive Comparison
                    val shuffledPoints = extractedPoints.shuffled(kotlin.random.Random(styleIndex * 31 + 7)).take(3)
                    val points = if (shuffledPoints.isNotEmpty()) {
                        shuffledPoints.mapIndexed { i, pt -> "Insight ${i + 1}: $pt" }.joinToString("\n")
                    } else {
                        "Insight 1: Primary architectural structure of $subject\nInsight 2: Comparative advantages"
                    }
                    buildString {
                        appendLine("Deep-Dive & Architecture:")
                        appendLine(points)
                        appendLine()
                        appendLine("Active Recall Challenge:")
                        appendLine("Q: When would you choose $keyTerm over alternative approaches, and why?")
                        appendLine()
                        appendLine("Study Plan:")
                        appendLine("Session: $estMins mins | Technique: Blurting (Write all details from memory)")
                    }.trimEnd()
                }
                else -> {
                    // Perspective 4: Rapid-Fire Flashcard Q&A (Front & Back)
                    val answerSummary = if (extractedPoints.isNotEmpty()) {
                        extractedPoints.take(3).joinToString("; ")
                    } else {
                        "Key mechanisms and rules of $subject"
                    }
                    buildString {
                        appendLine("Flashcard Q&A Mode:")
                        appendLine("FRONT (Prompt):")
                        appendLine("What are the defining characteristics and key rules of $subject?")
                        appendLine()
                        appendLine("BACK (Answer):")
                        appendLine(answerSummary)
                        appendLine()
                        appendLine("Self-Check:")
                        appendLine("Rate your recall: Easy (next in 3d) | Good (1d) | Hard (review today)")
                    }.trimEnd()
                }
            }
        }

    // ── Quiz Questions Generator ──────────────────────────────────────────────

    suspend fun generateQuizQuestions(note: StudyNote): Result<List<QuizQuestion>> =
        runCatching {
            kotlinx.coroutines.delay(600)
            val lines = note.aiFlashcardSummary
                .lines()
                .filter { it.length > 15 && !it.startsWith("Study Plan") && !it.startsWith("Key Concepts") && !it.startsWith("Estimated") }
                .take(3)
            if (lines.isEmpty()) throw IllegalStateException("Not enough content")
            lines.mapIndexed { i, line ->
                QuizQuestion(
                    id       = i,
                    question = "What does this concept in \"${note.subject}\" describe?\n\"${line.take(80)}…\"",
                    answer   = line,
                )
            }
        }

    // ── SM-2 Spaced Repetition ────────────────────────────────────────────────

    private fun scheduleNextReview(reviewCount: Int): Long {
        val days = when (reviewCount) {
            0    -> 1; 1 -> 3; 2 -> 7; 3 -> 14; else -> 30
        }
        return System.currentTimeMillis() + days * 86_400_000L
    }
}

data class QuizQuestion(val id: Int, val question: String, val answer: String)
