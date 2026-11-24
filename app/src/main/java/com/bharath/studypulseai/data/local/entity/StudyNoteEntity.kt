package com.bharath.studypulseai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val topicNotes: String,
    val aiFlashcardSummary: String,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val subjectColor: Long = 0xFF2563EB,
    val reviewCount: Int = 0,
    val difficulty: String = "MEDIUM",          // stores Difficulty enum name
    val nextReviewAt: Long? = null,
    val lastReviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
