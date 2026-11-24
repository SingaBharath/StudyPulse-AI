package com.bharath.studypulseai.data.mapper

import androidx.compose.ui.graphics.Color
import com.bharath.studypulseai.data.local.entity.StudyNoteEntity
import com.bharath.studypulseai.domain.model.Difficulty
import com.bharath.studypulseai.domain.model.StudyNote

object StudyNoteMapper {

    fun StudyNoteEntity.toDomain(): StudyNote = StudyNote(
        id                 = id,
        subject            = subject,
        topicNotes         = topicNotes,
        aiFlashcardSummary = aiFlashcardSummary,
        isCompleted        = isCompleted,
        isPinned           = isPinned,
        subjectColor       = Color(subjectColor),
        reviewCount        = reviewCount,
        difficulty         = runCatching { Difficulty.valueOf(difficulty) }.getOrDefault(Difficulty.MEDIUM),
        nextReviewAt       = nextReviewAt,
        lastReviewedAt     = lastReviewedAt,
        createdAt          = createdAt,
        updatedAt          = updatedAt,
    )

    fun StudyNote.toEntity(): StudyNoteEntity = StudyNoteEntity(
        id                 = id,
        subject            = subject,
        topicNotes         = topicNotes,
        aiFlashcardSummary = aiFlashcardSummary,
        isCompleted        = isCompleted,
        isPinned           = isPinned,
        subjectColor       = subjectColor.value.toLong(),
        reviewCount        = reviewCount,
        difficulty         = difficulty.name,
        nextReviewAt       = nextReviewAt,
        lastReviewedAt     = lastReviewedAt,
        createdAt          = createdAt,
        updatedAt          = updatedAt,
    )

    fun List<StudyNoteEntity>.toDomainList(): List<StudyNote> = map { it.toDomain() }
}
