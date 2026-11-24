package com.bharath.studypulseai.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bharath.studypulseai.domain.model.Difficulty
import com.bharath.studypulseai.domain.model.StudyNote
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudyNoteCard(
    note: StudyNote,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleComplete: () -> Unit,
    onDeleteRequest: () -> Unit,
    onReviewMark: () -> Unit,
    onTogglePin: () -> Unit,
    onRegenerateAi: () -> Unit,
    isRegenerating: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val createdLabel  = remember(note.createdAt) { dateFormatter.format(Date(note.createdAt)) }

    val (diffColor, diffText) = when (note.difficulty) {
        Difficulty.EASY   -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        Difficulty.HARD   -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else              -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(
            containerColor = if (note.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.isCompleted) 0.dp else 1.5.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {

            // Accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = if (note.isCompleted) MaterialTheme.colorScheme.outlineVariant
                                else note.subjectColor,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp),
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                // ── Row 1: Checkbox + Title + AI Toggle + Pin + Delete ────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier         = Modifier.weight(1f),
                    ) {
                        Checkbox(
                            checked         = note.isCompleted,
                            onCheckedChange = { onToggleComplete() },
                            colors          = CheckboxDefaults.colors(
                                checkedColor   = note.subjectColor,
                                checkmarkColor = Color.White,
                                uncheckedColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text           = note.subject,
                            style          = MaterialTheme.typography.titleMedium,
                            fontWeight     = FontWeight.Bold,
                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null,
                            color          = if (note.isCompleted)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines       = 1,
                            overflow       = TextOverflow.Ellipsis,
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // AI Flashcard toggle icon
                        IconButton(
                            onClick  = onToggleExpand,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                imageVector        = if (isExpanded) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = if (isExpanded) "Hide flashcard" else "Show flashcard",
                                tint               = if (isExpanded) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(18.dp),
                            )
                        }

                        // Pin toggle
                        IconButton(
                            onClick  = onTogglePin,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.PushPin,
                                contentDescription = if (note.isPinned) "Unpin" else "Pin",
                                tint               = if (note.isPinned) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.outlineVariant,
                                modifier           = Modifier.size(18.dp),
                            )
                        }

                        // Delete
                        IconButton(
                            onClick  = onDeleteRequest,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete",
                                tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier           = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                // ── Row 2: Badges ─────────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    DifficultyBadge(
                        label          = note.difficulty.label,
                        containerColor = diffColor,
                        contentColor   = diffText,
                    )

                    if (note.reviewCount > 0) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier              = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.Replay,
                                    null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(10.dp),
                                )
                                Text(
                                    "${note.reviewCount} reviews",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    if (note.isPinned) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                "Pinned",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            )
                        }
                    }
                }

                // ── Raw Notes Preview ─────────────────────────────────────────
                Text(
                    text     = note.topicNotes,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) 4 else 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // ── AI Flashcard Panel (Animated) ─────────────────────────────
                AnimatedVisibility(
                    visible = isExpanded,
                    enter   = expandVertically(tween(260)) + fadeIn(tween(260)),
                    exit    = shrinkVertically(tween(220)) + fadeOut(tween(180)),
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant,
                        )

                        // AI Flashcard container
                        Surface(
                            color    = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            shape    = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier            = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiary,
                                    ) {
                                        Icon(
                                            Icons.Outlined.AutoAwesome,
                                            contentDescription = null,
                                            tint     = MaterialTheme.colorScheme.onTertiary,
                                            modifier = Modifier.padding(5.dp).size(14.dp),
                                        )
                                    }
                                    Text(
                                        "AI Flashcard & Recall Plan",
                                        style      = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                }

                                Text(
                                    text  = note.aiFlashcardSummary.ifBlank { "Generating AI flashcard summary…" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }

                        // Action row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            modifier              = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedButton(
                                onClick        = onReviewMark,
                                modifier       = Modifier.weight(1f),
                                shape          = MaterialTheme.shapes.small,
                                contentPadding = PaddingValues(vertical = 8.dp),
                                colors         = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                                border         = androidx.compose.foundation.BorderStroke(
                                    1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                ),
                            ) {
                                Icon(Icons.Outlined.Replay, null, Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Log Review", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick        = onRegenerateAi,
                                enabled        = !isRegenerating,
                                modifier       = Modifier.weight(1.1f),
                                shape          = MaterialTheme.shapes.small,
                                contentPadding = PaddingValues(vertical = 8.dp),
                                colors         = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.secondary,
                                ),
                                border         = androidx.compose.foundation.BorderStroke(
                                    1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                                ),
                            ) {
                                if (isRegenerating) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color       = MaterialTheme.colorScheme.secondary,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Regenerating…", style = MaterialTheme.typography.labelSmall)
                                } else {
                                    Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Regenerate", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            if (note.isCompleted) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.weight(0.9f),
                                ) {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                    ) {
                                        Icon(
                                            Icons.Outlined.TaskAlt,
                                            null,
                                            tint     = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(15.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Mastered",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Footer metadata ───────────────────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text  = "Added $createdLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )

                    TextButton(
                        onClick        = onToggleExpand,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text  = if (isExpanded) "Hide flashcard" else "View AI flashcard",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            modifier    = Modifier.size(14.dp),
                            tint        = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
