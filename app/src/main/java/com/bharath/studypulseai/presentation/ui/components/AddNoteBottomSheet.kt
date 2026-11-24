package com.bharath.studypulseai.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.bharath.studypulseai.domain.model.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteBottomSheet(
    isGenerating: Boolean,
    onSave: (subject: String, notes: String, difficulty: Difficulty) -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager   = LocalFocusManager.current

    var subject        by remember { mutableStateOf("") }
    var rawNotes       by remember { mutableStateOf("") }
    var difficulty     by remember { mutableStateOf(Difficulty.MEDIUM) }
    var subjectError   by remember { mutableStateOf(false) }
    var notesError     by remember { mutableStateOf(false) }
    var hasSubmitted   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    // When generation finishes after the user clicked Save, auto-dismiss the sheet
    LaunchedEffect(isGenerating) {
        if (hasSubmitted && !isGenerating) {
            onDismiss()
        }
    }

    fun validate(): Boolean {
        subjectError = subject.isBlank()
        notesError   = rawNotes.isBlank()
        return !subjectError && !notesError
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle       = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "New Study Note",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "AI will analyze your notes and generate an active recall flashcard",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Subject ───────────────────────────────────────────────────────
            OutlinedTextField(
                value            = subject,
                onValueChange    = { subject = it; subjectError = false },
                label            = { Text("Subject or unit title") },
                placeholder      = { Text("e.g. Data Structures — Unit 3") },
                leadingIcon      = {
                    Icon(Icons.Outlined.MenuBook, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                isError          = subjectError,
                supportingText   = if (subjectError) { { Text("Subject is required") } } else null,
                singleLine       = true,
                enabled          = !isGenerating,
                keyboardOptions  = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Next,
                ),
                modifier         = Modifier.fillMaxWidth().focusRequester(focusRequester),
                shape            = MaterialTheme.shapes.small,
                colors           = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            // ── Notes ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value            = rawNotes,
                onValueChange    = { rawNotes = it; notesError = false },
                label            = { Text("Lecture notes or key concepts") },
                placeholder      = { Text("Paste or type definitions, rules, formulas, or concepts…") },
                leadingIcon      = {
                    Icon(Icons.Outlined.EditNote, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                isError          = notesError,
                supportingText   = if (notesError) { { Text("Notes content is required") } } else null,
                minLines         = 4,
                maxLines         = 8,
                enabled          = !isGenerating,
                keyboardOptions  = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Done,
                ),
                keyboardActions  = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (validate() && !isGenerating) {
                            hasSubmitted = true
                            onSave(subject.trim(), rawNotes.trim(), difficulty)
                        }
                    },
                ),
                modifier         = Modifier.fillMaxWidth(),
                shape            = MaterialTheme.shapes.small,
                colors           = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            // ── Difficulty Selector ───────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Difficulty",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Difficulty.entries.forEach { d ->
                        val selected = difficulty == d
                        val (bg, fg) = when (d) {
                            Difficulty.EASY   -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                            Difficulty.MEDIUM -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                            Difficulty.HARD   -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                        }
                        FilterChip(
                            selected = selected,
                            enabled  = !isGenerating,
                            onClick  = { difficulty = d },
                            label    = { Text(d.label, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Outlined.Check, null, Modifier.size(14.dp)) }
                            } else null,
                            shape    = MaterialTheme.shapes.extraLarge,
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor      = bg,
                                selectedLabelColor          = fg,
                                selectedLeadingIconColor    = fg,
                            ),
                        )
                    }
                }
            }

            // ── Info banner ───────────────────────────────────────────────────
            Surface(
                color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape    = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier              = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        "AI extracts key takeaways, creates an active recall challenge, and sets your spaced repetition schedule.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // ── Buttons ───────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick  = onDismiss,
                    enabled  = !isGenerating,
                    modifier = Modifier.weight(1f),
                    shape    = MaterialTheme.shapes.small,
                ) { Text("Cancel") }

                Button(
                    onClick  = {
                        focusManager.clearFocus()
                        if (validate() && !isGenerating) {
                            hasSubmitted = true
                            onSave(subject.trim(), rawNotes.trim(), difficulty)
                        }
                    },
                    enabled  = !isGenerating,
                    modifier = Modifier.weight(2f),
                    shape    = MaterialTheme.shapes.small,
                ) {
                    if (isGenerating) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text("Generating Flashcard…")
                        }
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(16.dp))
                            Text("Generate & Save")
                        }
                    }
                }
            }
        }
    }
}
