package com.bharath.studypulseai.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bharath.studypulseai.data.local.dao.StudyNoteDao
import com.bharath.studypulseai.data.local.entity.StudyNoteEntity

@Database(entities = [StudyNoteEntity::class], version = 3, exportSchema = false)
abstract class StudyPulseDatabase : RoomDatabase() {

    abstract fun studyNoteDao(): StudyNoteDao

    companion object {
        const val DB_NAME = "studypulse_v3.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_notes ADD COLUMN subjectColor INTEGER NOT NULL DEFAULT 0xFF2563EB")
                db.execSQL("ALTER TABLE study_notes ADD COLUMN reviewCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE study_notes ADD COLUMN nextReviewAt INTEGER")
                db.execSQL("ALTER TABLE study_notes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE study_notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE study_notes ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'MEDIUM'")
                db.execSQL("ALTER TABLE study_notes ADD COLUMN lastReviewedAt INTEGER")
            }
        }

        @Volatile private var INSTANCE: StudyPulseDatabase? = null

        fun getInstance(context: Context): StudyPulseDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyPulseDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
