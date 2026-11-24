package com.bharath.studypulseai.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Shimmer Skeleton ──────────────────────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant,
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue  = -800f,
        targetValue   = 800f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label         = "shimmer_x",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    shimmerColors,
                    start = Offset(translateX, 0f),
                    end   = Offset(translateX + 400f, 0f),
                )
            )
    )
}

@Composable
fun NoteCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ShimmerBox(
                modifier = Modifier.width(4.dp).height(100.dp),
                shape    = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
            )
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    ShimmerBox(modifier = Modifier.size(18.dp), shape = CircleShape)
                    ShimmerBox(modifier = Modifier.height(16.dp).fillMaxWidth(0.52f))
                }
                ShimmerBox(modifier = Modifier.height(13.dp).fillMaxWidth(0.88f))
                ShimmerBox(modifier = Modifier.height(13.dp).fillMaxWidth(0.72f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.height(24.dp).width(70.dp), shape = MaterialTheme.shapes.extraLarge)
                    ShimmerBox(modifier = Modifier.height(24.dp).width(90.dp), shape = MaterialTheme.shapes.extraLarge)
                }
            }
        }
    }
}

// ── Circular Progress with Label ──────────────────────────────────────────────

@Composable
fun RingProgress(
    progress: Float,
    label: String,
    size: Dp = 72.dp,
    strokeWidth: Dp = 7.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.outlineVariant,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        CircularProgressIndicator(
            progress    = { progress },
            modifier    = Modifier.fillMaxSize(),
            color       = color,
            trackColor  = trackColor,
            strokeWidth = strokeWidth,
            strokeCap   = StrokeCap.Round,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Outlined.Info,
    title: String = "Nothing here yet",
    subtitle: String = "Tap the button below to get started",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape  = CircleShape,
            color  = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Difficulty Badge ──────────────────────────────────────────────────────────

@Composable
fun DifficultyBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = containerColor) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text      = title.uppercase(),
        style     = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
        modifier  = modifier.padding(horizontal = 2.dp, vertical = 4.dp),
    )
}
