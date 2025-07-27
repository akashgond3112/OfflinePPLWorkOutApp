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
import com.example.offlinepplworkoutapp.data.entity.Exercise
import com.example.offlinepplworkoutapp.data.entity.WorkoutDay
import com.example.offlinepplworkoutapp.data.entity.WorkoutEntry
import com.example.offlinepplworkoutapp.data.entity.SetEntry
import com.example.offlinepplworkoutapp.data.ExerciseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    entities = [Exercise::class, WorkoutDay::class, WorkoutEntry::class, SetEntry::class],
    version = 6,  // Updated from 5 to 6
    exportSchema = false
)
abstract class PPLWorkoutDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDayDao(): WorkoutDayDao
    abstract fun workoutEntryDao(): WorkoutEntryDao
    abstract fun setEntryDao(): SetEntryDao

    private class PPLWorkoutDatabaseCallback : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.exerciseDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Ensure exercises are populated even if database already exists
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val exerciseCount = database.exerciseDao().getExerciseCount()
                    if (exerciseCount == 0) {
                        populateDatabase(database.exerciseDao())
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
    }

    companion object {
        @Volatile
        private var INSTANCE: PPLWorkoutDatabase? = null

        // Migration from version 5 to 6 - Add new Exercise fields
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("🔄 MIGRATION: Starting migration from v5 to v6...")

                // Add new columns to exercises table with default values
                database.execSQL("ALTER TABLE exercises ADD COLUMN primaryMuscle TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE exercises ADD COLUMN secondaryMuscles TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE exercises ADD COLUMN equipment TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE exercises ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'Intermediate'")
                database.execSQL("ALTER TABLE exercises ADD COLUMN instructions TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE exercises ADD COLUMN tips TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE exercises ADD COLUMN category TEXT NOT NULL DEFAULT ''")

                println("🔄 MIGRATION: Successfully added new columns to exercises table")
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
                    .addMigrations(MIGRATION_5_6)  // Fixed: use addMigrations instead of addMigration
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
