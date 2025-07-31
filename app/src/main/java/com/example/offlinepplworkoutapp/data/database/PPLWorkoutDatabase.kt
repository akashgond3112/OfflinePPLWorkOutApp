package com.example.offlinepplworkoutapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.offlinepplworkoutapp.data.dao.ExerciseDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutDayDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryDao
import com.example.offlinepplworkoutapp.data.dao.SetEntryDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutTemplateDao
import com.example.offlinepplworkoutapp.data.dao.TemplateExerciseDao
import com.example.offlinepplworkoutapp.data.entity.Exercise
import com.example.offlinepplworkoutapp.data.entity.WorkoutDay
import com.example.offlinepplworkoutapp.data.entity.WorkoutEntry
import com.example.offlinepplworkoutapp.data.entity.SetEntry
import com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate
import com.example.offlinepplworkoutapp.data.entity.TemplateExercise
import com.example.offlinepplworkoutapp.data.ExerciseData
import com.example.offlinepplworkoutapp.data.PPLTemplateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    entities = [Exercise::class, WorkoutDay::class, WorkoutEntry::class, SetEntry::class, WorkoutTemplate::class, TemplateExercise::class],
    version = 8,  // 🚀 NEW: Updated from 7 to 8 for set performance data fields
    exportSchema = false
)
abstract class PPLWorkoutDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDayDao(): WorkoutDayDao
    abstract fun workoutEntryDao(): WorkoutEntryDao
    abstract fun setEntryDao(): SetEntryDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao

    private class PPLWorkoutDatabaseCallback : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.exerciseDao())
                    populateTemplates(database.workoutTemplateDao(), database.templateExerciseDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Ensure exercises and templates are populated even if database already exists
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val exerciseCount = database.exerciseDao().getExerciseCount()
                    if (exerciseCount == 0) {
                        populateDatabase(database.exerciseDao())
                    }

                    val templateCount = database.workoutTemplateDao().getTemplateCount()
                    if (templateCount == 0) {
                        populateTemplates(database.workoutTemplateDao(), database.templateExerciseDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(exerciseDao: ExerciseDao) {
            // Clear existing data
            exerciseDao.deleteAll()

            // Insert PPL exercises
            val exercises = getPPLExercises()
            exerciseDao.insertAll(exercises)
        }

        suspend fun populateTemplates(templateDao: WorkoutTemplateDao, templateExerciseDao: TemplateExerciseDao) {
            // Clear existing template data
            templateExerciseDao.deleteAll()
            templateDao.deleteAll()

            // Insert PPL templates
            val templates = PPLTemplateData.getPPLTemplates()
            templateDao.insertTemplates(templates)

            // Insert template exercises
            val templateExercises = PPLTemplateData.getTemplateExercises()
            templateExerciseDao.insertTemplateExercises(templateExercises)

            println("🏋️ DATABASE: Successfully populated ${templates.size} templates with ${templateExercises.size} template exercises")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PPLWorkoutDatabase? = null

        // Migration from version 6 to 7 - Add WorkoutTemplate and TemplateExercise tables
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("🔄 MIGRATION: Starting migration from v6 to v7...")

                // Create WorkoutTemplate table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workout_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `estimatedDuration` INTEGER NOT NULL,
                        `difficulty` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `isCustom` INTEGER NOT NULL DEFAULT 0,
                        `isActive` INTEGER NOT NULL DEFAULT 1,
                        `createdDate` TEXT NOT NULL DEFAULT '',
                        `lastUsedDate` TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                // Create TemplateExercise table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `template_exercises` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `templateId` INTEGER NOT NULL,
                        `exerciseId` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        `sets` INTEGER NOT NULL,
                        `reps` INTEGER NOT NULL,
                        `restSeconds` INTEGER NOT NULL,
                        `weight` REAL NOT NULL DEFAULT 0.0,
                        `notes` TEXT NOT NULL DEFAULT '',
                        `isSuperset` INTEGER NOT NULL DEFAULT 0,
                        `supersetGroup` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())

                println("🔄 MIGRATION: Successfully created WorkoutTemplate and TemplateExercise tables")
            }
        }

        // 🚀 NEW: Migration from version 7 to 8 - Add set performance data fields
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("🔄 MIGRATION: Starting migration from v7 to v8...")

                // Add reps_performed column to set_entries table
                database.execSQL("""
                    ALTER TABLE set_entries ADD COLUMN reps_performed INTEGER
                """.trimIndent())

                // Add weight_used column to set_entries table
                database.execSQL("""
                    ALTER TABLE set_entries ADD COLUMN weight_used REAL
                """.trimIndent())

                println("🔄 MIGRATION: Successfully added reps_performed and weight_used columns to set_entries table")
                println("🚀 MIGRATION v7→v8: Set performance data fields are now available!")
            }
        }

        fun getDatabase(context: Context): PPLWorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PPLWorkoutDatabase::class.java,
                    "ppl_workout_database"
                )
                    .addCallback(PPLWorkoutDatabaseCallback())
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)  // Fixed: use addMigrations instead of addMigration
                    .fallbackToDestructiveMigration()
                    // Force database recreation to ensure clean state
                    .build()

                INSTANCE = instance
                instance
            }
        }

        // Reset database method - clears workout progress while preserving exercise definitions
        suspend fun resetDatabase() {
            INSTANCE?.let { database ->
                // Clear all user workout data directly (we're already in a suspend function)
                database.workoutDayDao().deleteAll()
                database.workoutEntryDao().deleteAll()
                database.setEntryDao().deleteAll()

                // Force close and invalidate all active connections to clear cache
                database.clearAllTables()
                database.invalidationTracker.refreshVersionsAsync()

                // Note: We're not deleting exercises since that would remove the exercise library
            }
        }

        // Method to completely reset the database instance (more aggressive)
        suspend fun forceResetDatabase(context: Context) {
            println("🔧 RESET: Starting forceResetDatabase...")

            INSTANCE?.let { database ->
                println("🔧 RESET: Clearing all data from existing database...")

                try {
                    // Run database operations on IO dispatcher to avoid main thread issues
                    withContext(Dispatchers.IO) {
                        // Clear all data and log the results
                        val workoutDaysDeleted = database.workoutDayDao().deleteAll()
                        val workoutEntriesDeleted = database.workoutEntryDao().deleteAll()
                        val setEntriesDeleted = database.setEntryDao().deleteAll()

                        println("🔧 RESET: Deleted $workoutDaysDeleted workout days")
                        println("🔧 RESET: Deleted $workoutEntriesDeleted workout entries")
                        println("🔧 RESET: Deleted $setEntriesDeleted set entries")

                        // Use the public API for invalidation instead of restricted refreshVersionsSync
                        database.invalidationTracker.refreshVersionsAsync()
                        println("🔧 RESET: Invalidated all cached queries (async)")

                        // Clear all tables to reset Room's internal state
                        database.clearAllTables()
                        println("🔧 RESET: Cleared all Room tables")
                    }

                } catch (e: Exception) {
                    println("🔧 RESET ERROR: ${e.message}")

                    // If we get errors, fall back to recreating the database
                    try {
                        database.close()
                        println("🔧 RESET: Closed database due to errors")
                    } catch (closeError: Exception) {
                        println("🔧 RESET: Error closing database: ${closeError.message}")
                    }

                    // Clear the singleton instance to force recreation
                    INSTANCE = null
                    println("🔧 RESET: Database instance cleared for recreation")
                }
            }

            if (INSTANCE == null) {
                // Force garbage collection to clear any remaining references
                System.gc()
                println("🔧 RESET: Garbage collection forced")

                // Recreate the database instance
                getDatabase(context)
                println("🔧 RESET: Database recreated")
            }

            println("🔧 RESET: Reset complete")
        }

        // Method to verify database is empty (for debugging)
        suspend fun verifyDatabaseEmpty(): Triple<Int, Int, Int> {
            return INSTANCE?.let { database ->
                val workoutDays = database.workoutDayDao().getWorkoutDayCount()
                val workoutEntries = database.workoutEntryDao().getWorkoutEntryCount()
                val setEntries = database.setEntryDao().getSetEntryCount()
                println("🔍 VERIFY: WorkoutDays: $workoutDays, WorkoutEntries: $workoutEntries, SetEntries: $setEntries")
                Triple(workoutDays, workoutEntries, setEntries)
            } ?: Triple(0, 0, 0)
        }

        // Method to force populate exercises when they're missing
        suspend fun forcePopulateExercises() {
            INSTANCE?.let { database ->
                println("🏋️ DATABASE: Force populating exercises...")
                try {
                    val exerciseDao = database.exerciseDao()
                    val exercises = ExerciseData.getPPLExercises() // Use new rich exercise data

                    // Clear any existing exercises and insert fresh ones
                    exerciseDao.deleteAll()
                    exerciseDao.insertAll(exercises)

                    println("🏋️ DATABASE: Successfully inserted ${exercises.size} exercises")
                } catch (e: Exception) {
                    println("🏋️ DATABASE ERROR: Failed to populate exercises - ${e.message}")
                }
            }
        }

        private fun getPPLExercises(): List<Exercise> {
            // Delegate to the new ExerciseData class for better organization
            return ExerciseData.getPPLExercises()
        }
    }
}
