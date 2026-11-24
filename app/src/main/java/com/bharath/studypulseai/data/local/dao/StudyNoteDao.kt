package com.bharath.studypulseai.data.local.dao

import androidx.room.*
import com.bharath.studypulseai.data.local.entity.StudyNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyNoteDao {

    // ── Reads ─────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM study_notes ORDER BY isPinned DESC, createdAt DESC")
    fun observeAll(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE isCompleted = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun observePending(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun observeCompleted(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE isPinned = 1 ORDER BY createdAt DESC")
    fun observePinned(): Flow<List<StudyNoteEntity>>

    @Query("""
        SELECT * FROM study_notes
        WHERE nextReviewAt IS NOT NULL AND nextReviewAt <= :now AND isCompleted = 0
        ORDER BY nextReviewAt ASC
    """)
    fun observeDueForReview(now: Long): Flow<List<StudyNoteEntity>>

    @Query("""
        SELECT * FROM study_notes
        WHERE subject LIKE '%' || :q || '%'
           OR topicNotes LIKE '%' || :q || '%'
        ORDER BY isPinned DESC, createdAt DESC
    """)
    fun search(q: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): StudyNoteEntity?

    @Query("SELECT COUNT(*) FROM study_notes")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_notes WHERE isCompleted = 1")
    fun observeCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_notes WHERE nextReviewAt IS NOT NULL AND nextReviewAt <= :now AND isCompleted = 0")
    fun observeDueCount(now: Long): Flow<Int>

    @Query("SELECT SUM(reviewCount) FROM study_notes")
    fun observeTotalReviews(): Flow<Int?>

    // ── Writes ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: StudyNoteEntity): Long

    @Update
    suspend fun update(note: StudyNoteEntity)

    @Delete
    suspend fun delete(note: StudyNoteEntity)

    @Query("UPDATE study_notes SET isCompleted = :done, updatedAt = :now WHERE id = :id")
    suspend fun setCompleted(id: Int, done: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE study_notes SET isPinned = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinned(id: Int, pinned: Boolean, now: Long = System.currentTimeMillis())

    @Query("""
        UPDATE study_notes
        SET reviewCount = reviewCount + 1,
            lastReviewedAt = :now,
            nextReviewAt = :next,
            updatedAt = :now
        WHERE id = :id
    """)
    suspend fun recordReview(id: Int, next: Long, now: Long = System.currentTimeMillis())
}
