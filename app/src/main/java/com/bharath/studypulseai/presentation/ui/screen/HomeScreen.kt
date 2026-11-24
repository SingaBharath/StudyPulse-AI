package com.bharath.studypulseai.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharath.studypulseai.domain.model.NoteFilter
import com.bharath.studypulseai.domain.model.SortOrder
import com.bharath.studypulseai.domain.model.StudyNote
import com.bharath.studypulseai.domain.model.StudyStats
import com.bharath.studypulseai.presentation.ui.components.*
import com.bharath.studypulseai.presentation.viewmodel.NotesUiState
import com.bharath.studypulseai.presentation.viewmodel.StudyViewModel
import com.bharath.studypulseai.presentation.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: StudyViewModel) {

    val notesState   by viewModel.notesUiState.collectAsState()
    val stats        by viewModel.stats.collectAsState()
    val isGenerating by viewModel.isGeneratingAi.collectAsState()
    val regeneratingNoteId by viewModel.regeneratingNoteId.collectAsState()
    val searchQuery  by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val sortOrder    by viewModel.sortOrder.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState         = rememberLazyListState()
    val showExtendedFab   by remember { derivedStateOf { !listState.isScrollInProgress } }

    var expandedNoteId by rememberSaveable { mutableStateOf<Int?>(null) }
    var showAddSheet   by remember { mutableStateOf(false) }
    var noteToDelete   by remember { mutableStateOf<StudyNote?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSortMenu   by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var showQuizSheet  by remember { mutableStateOf(false) }
    var quizNote       by remember { mutableStateOf<StudyNote?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(
                        message     = event.message,
                        actionLabel = event.actionLabel,
                        duration    = SnackbarDuration.Short,
                    )
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost   = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData   = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor   = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor    = MaterialTheme.colorScheme.primary,
                    shape          = MaterialTheme.shapes.small,
                )
            }
        },
        topBar = {
            StudyPulseTopBar(
                isSearchActive = isSearchActive,
                searchQuery    = searchQuery,
                onSearchQuery  = viewModel::onSearchQueryChange,
                onSearchToggle = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) viewModel.onSearchQueryChange("")
                },
                onSortClick    = { showSortMenu = true },
                onStatsClick   = { showStatsSheet = true },
                sortMenu       = {
                    SortDropdownMenu(
                        expanded    = showSortMenu,
                        currentSort = sortOrder,
                        onSelect    = { viewModel.onSortChange(it); showSortMenu = false },
                        onDismiss   = { showSortMenu = false },
                    )
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSearchActive,
                enter   = scaleIn(tween(200)) + fadeIn(tween(200)),
                exit    = scaleOut(tween(150)) + fadeOut(tween(150)),
            ) {
                ExtendedFloatingActionButton(
                    onClick        = { showAddSheet = true },
                    icon           = { Icon(Icons.Outlined.Add, "New note") },
                    text           = { Text("New Study Note") },
                    expanded       = showExtendedFab,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    shape          = MaterialTheme.shapes.large,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pv ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = pv.calculateTopPadding(), bottom = pv.calculateBottomPadding())
        ) {
            LazyColumn(
                state          = listState,
                contentPadding = PaddingValues(
                    top    = 8.dp,
                    bottom = 100.dp,
                    start  = 16.dp,
                    end    = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier            = Modifier.fillMaxSize(),
            ) {

            // ── Progress dashboard ────────────────────────────────────────────
            item(key = "dashboard") {
                ProgressDashboard(
                    stats        = stats,
                    onStatsClick = { showStatsSheet = true },
                )
            }

            // ── Due today banner ──────────────────────────────────────────────
            if (stats.dueForReview > 0) {
                item(key = "due_banner") {
                    DueTodayBanner(count = stats.dueForReview) {
                        viewModel.onFilterChange(NoteFilter.DUE)
                    }
                }
            }

            // ── Filter + Sort row ─────────────────────────────────────────────
            item(key = "filters") {
                FilterRow(
                    activeFilter   = activeFilter,
                    onFilterChange = viewModel::onFilterChange,
                    stats          = stats,
                )
            }

            // ── Notes ─────────────────────────────────────────────────────────
            when (val state = notesState) {
                is NotesUiState.Loading -> {
                    items(4, key = { "sk_$it" }) { NoteCardSkeleton() }
                }
                is NotesUiState.Empty -> {
                    item(key = "empty") {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 56.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon     = if (searchQuery.isNotBlank()) Icons.Outlined.SearchOff
                                           else Icons.Outlined.LibraryBooks,
                                title    = if (searchQuery.isNotBlank()) "No results for \"$searchQuery\""
                                           else "No study topics yet",
                                subtitle = if (searchQuery.isNotBlank()) "Try a different search term"
                                           else "Tap the button below to create your first AI-powered flashcard",
                            )
                        }
                    }
                }
                is NotesUiState.Success -> {
                    items(
                        items       = state.notes,
                        key         = { it.id },
                        contentType = { "note" },
                    ) { note ->
                        StudyNoteCard(
                            note             = note,
                            isExpanded       = expandedNoteId == note.id,
                            onToggleExpand   = {
                                expandedNoteId = if (expandedNoteId == note.id) null else note.id
                            },
                            onToggleComplete = { viewModel.toggleCompletion(note) },
                            onDeleteRequest  = { noteToDelete = note },
                            onReviewMark     = { viewModel.markReviewed(note) },
                            onTogglePin      = { viewModel.togglePin(note) },
                            onRegenerateAi   = { viewModel.regenerateFlashcard(note) },
                            isRegenerating   = regeneratingNoteId == note.id,
                        )
                    }
                }
                is NotesUiState.Error -> {
                    item(key = "error") { ErrorBanner(state.message) }
                }
            }
        }

        if (isGenerating) {
            LinearProgressIndicator(
                modifier   = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color      = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
}

    // ── Add Note sheet ────────────────────────────────────────────────────────
    if (showAddSheet) {
        AddNoteBottomSheet(
            isGenerating = isGenerating,
            onSave       = { subject, notes, difficulty ->
                viewModel.addNoteWithAi(subject, notes, difficulty)
            },
            onDismiss    = { showAddSheet = false },
        )
    }

    // ── Stats sheet ───────────────────────────────────────────────────────────
    if (showStatsSheet) {
        StatsBottomSheet(stats = stats, onDismiss = { showStatsSheet = false })
    }

    // ── Delete dialog ─────────────────────────────────────────────────────────
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            icon             = {
                Icon(
                    Icons.Outlined.DeleteOutline, null,
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp),
                )
            },
            title = { Text("Remove topic?") },
            text  = {
                Text(
                    "\"${note.subject}\" will be permanently deleted from this device.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteNote(note); noteToDelete = null },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError,
                    ),
                    shape   = MaterialTheme.shapes.small,
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("Keep") }
            },
            shape = MaterialTheme.shapes.large,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private composables
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyPulseTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQuery: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onSortClick: () -> Unit,
    onStatsClick: () -> Unit,
    sortMenu: @Composable () -> Unit,
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                label = "topbar",
            ) { searching ->
                if (searching) {
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = onSearchQuery,
                        placeholder   = { Text("Search topics, notes…") },
                        singleLine    = true,
                        leadingIcon   = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = MaterialTheme.shapes.small,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                } else {
                    Text(
                        "StudyPulse AI",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        actions = {
            Box {
                sortMenu()
                IconButton(onClick = onSortClick) {
                    Icon(Icons.Outlined.SwapVert, "Sort", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onSearchToggle) {
                Icon(
                    if (isSearchActive) Icons.Outlined.Close else Icons.Outlined.Search,
                    if (isSearchActive) "Close" else "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentSort: SortOrder,
    onSelect: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        SortOrder.entries.forEach { order ->
            DropdownMenuItem(
                text          = { Text(order.label, style = MaterialTheme.typography.bodyMedium) },
                onClick       = { onSelect(order) },
                leadingIcon   = if (order == currentSort) {
                    { Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                } else null,
            )
        }
    }
}

@Composable
private fun ProgressDashboard(stats: StudyStats, onStatsClick: () -> Unit) {
    val animProg by animateFloatAsState(
        targetValue   = stats.progressFraction,
        animationSpec = tween(900, easing = EaseOutCubic),
        label         = "progress",
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick   = onStatsClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        0f to MaterialTheme.colorScheme.primaryContainer,
                        1f to MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    )
                )
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Study Progress",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "${stats.completed} of ${stats.total} topics mastered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        )
                    }
                    RingProgress(
                        progress   = animProg,
                        label      = "${(animProg * 100).toInt()}%",
                        size       = 64.dp,
                        strokeWidth = 6.dp,
                        color      = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    )
                }

                LinearProgressIndicator(
                    progress    = { animProg },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color       = MaterialTheme.colorScheme.primary,
                    trackColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    strokeCap   = StrokeCap.Round,
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    StatItem("Total",    "${stats.total}",           Icons.Outlined.LibraryBooks)
                    VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatItem("Pending",  "${stats.pendingCount}",    Icons.Outlined.HourglassEmpty)
                    VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatItem("Mastered", "${stats.completed}",       Icons.Outlined.TaskAlt)
                    VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatItem("Reviews",  "${stats.totalReviews}",    Icons.Outlined.Replay)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            icon, null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            value,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun DueTodayBanner(count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color   = MaterialTheme.colorScheme.tertiaryContainer,
        shape   = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary) {
                    Icon(
                        Icons.Outlined.NotificationsActive, null,
                        tint     = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(6.dp).size(16.dp),
                    )
                }
                Column {
                    Text(
                        "$count topic${if (count > 1) "s" else ""} due for review",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        "Tap to see due topics",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
            Icon(
                Icons.Outlined.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun FilterRow(
    activeFilter: NoteFilter,
    onFilterChange: (NoteFilter) -> Unit,
    stats: StudyStats,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        NoteFilter.entries.forEach { filter ->
            val count = when (filter) {
                NoteFilter.ALL       -> stats.total
                NoteFilter.PENDING   -> stats.pendingCount
                NoteFilter.COMPLETED -> stats.completed
                NoteFilter.PINNED    -> null
                NoteFilter.DUE       -> stats.dueForReview
            }
            FilterChip(
                selected    = activeFilter == filter,
                onClick     = { onFilterChange(filter) },
                label       = {
                    Text(
                        if (count != null) "${filter.label} ($count)" else filter.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                leadingIcon = if (activeFilter == filter) {
                    { Icon(Icons.Outlined.Check, null, Modifier.size(14.dp)) }
                } else null,
                shape       = MaterialTheme.shapes.extraLarge,
                colors      = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        color    = MaterialTheme.colorScheme.errorContainer,
        shape    = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}
