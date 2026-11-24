package com.bharath.studypulseai.domain.model

import androidx.compose.ui.graphics.Color
import com.bharath.studypulseai.ui.theme.SubjectBlue

/**
 * Domain model — UI-layer view of a study note.
 * Intentionally decoupled from Room entity so schema changes never break the UI.
 */
data class StudyNote(
    val id: Int = 0,
    val subject: String,
    val topicNotes: String,
    val aiFlashcardSummary: String,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val subjectColor: Color = SubjectBlue,
    val reviewCount: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val nextReviewAt: Long? = null,
    val lastReviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class Difficulty(val label: String) {
    EASY("Easy"), MEDIUM("Medium"), HARD("Hard")
}

/** Aggregate stats for the dashboard. */
data class StudyStats(
    val total: Int = 0,
    val completed: Int = 0,
    val totalReviews: Int = 0,
    val dueForReview: Int = 0,
) {
    val progressFraction: Float
        get() = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    val pendingCount: Int get() = total - completed
}

/** Filter tabs for the main list. */
enum class NoteFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    COMPLETED("Mastered"),
    PINNED("Pinned"),
    DUE("Due Today"),
}

/** Sort options for the note list. */
enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    ALPHABETICAL("A → Z"),
    DIFFICULTY("By Difficulty"),
    REVIEW_COUNT("Most Reviewed"),
}
