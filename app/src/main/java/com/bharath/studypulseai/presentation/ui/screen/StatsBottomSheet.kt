package com.bharath.studypulseai.presentation.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharath.studypulseai.domain.model.StudyStats
import com.bharath.studypulseai.presentation.ui.components.RingProgress

/**
 * Full-detail stats bottom sheet.
 * No emojis — all visual cues via Material icons and colour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsBottomSheet(
    stats: StudyStats,
    onDismiss: () -> Unit,
) {
    val animProg by animateFloatAsState(
        targetValue   = stats.progressFraction,
        animationSpec = tween(900, easing = EaseOutCubic),
        label         = "stats_progress",
    )

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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // Header
            Text(
                "Study Analytics",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Ring progress centred
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                RingProgress(
                    progress    = animProg,
                    label       = "${(animProg * 100).toInt()}%",
                    size        = 120.dp,
                    strokeWidth = 10.dp,
                    color       = MaterialTheme.colorScheme.primary,
                    trackColor  = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            // Linear bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Overall mastery",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${stats.completed} / ${stats.total}",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )
                }
                LinearProgressIndicator(
                    progress    = { animProg },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color       = MaterialTheme.colorScheme.primary,
                    trackColor  = MaterialTheme.colorScheme.outlineVariant,
                    strokeCap   = StrokeCap.Round,
                )
            }

            // Stat cards grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        icon  = Icons.Outlined.LibraryBooks,
                        label = "Total Topics",
                        value = "${stats.total}",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        onColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        icon  = Icons.Outlined.TaskAlt,
                        label = "Mastered",
                        value = "${stats.completed}",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        icon  = Icons.Outlined.HourglassEmpty,
                        label = "Pending",
                        value = "${stats.pendingCount}",
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        icon  = Icons.Outlined.Replay,
                        label = "Total Reviews",
                        value = "${stats.totalReviews}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        icon  = Icons.Outlined.NotificationsActive,
                        label = "Due Today",
                        value = "${stats.dueForReview}",
                        color = if (stats.dueForReview > 0)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        onColor = if (stats.dueForReview > 0)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        icon  = Icons.Outlined.TrendingUp,
                        label = "Completion Rate",
                        value = "${(stats.progressFraction * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        onColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.small,
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    onColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color    = color,
        shape    = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, null, tint = onColor, modifier = Modifier.size(20.dp))
            Text(
                value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = onColor,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = onColor.copy(alpha = 0.75f),
            )
        }
    }
}
