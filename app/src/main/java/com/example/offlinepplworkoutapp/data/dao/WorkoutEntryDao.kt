package com.example.offlinepplworkoutapp.data.dao

import androidx.room.*
import com.example.offlinepplworkoutapp.data.entity.WorkoutEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutEntryDao {

    @Query("""
        SELECT we.id, we.day_id as dayId, we.exercise_id as exerciseId, we.sets, we.reps, we.isCompleted, we.totalSecondsSpent,
               e.name as exerciseName, e.isCompound, we.completedAt
        FROM workout_entries we
        INNER JOIN exercises e ON we.exercise_id = e.id
        WHERE we.day_id = :dayId
        ORDER BY we.id
    """)
    fun getWorkoutEntriesForDay(dayId: Int): Flow<List<WorkoutEntryWithExercise>>

    @Query("""
        SELECT we.id, we.day_id as dayId, we.exercise_id as exerciseId, we.sets, we.reps, we.isCompleted, we.totalSecondsSpent,
               e.name as exerciseName, e.isCompound, we.completedAt
        FROM workout_entries we
        INNER JOIN exercises e ON we.exercise_id = e.id
        WHERE we.day_id = :dayId
        ORDER BY we.id
    """)
    suspend fun getWorkoutEntriesForDaySync(dayId: Int): List<WorkoutEntryWithExercise>

    @Query("SELECT * FROM workout_entries WHERE id = :id")
    suspend fun getWorkoutEntryById(id: Int): WorkoutEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WorkoutEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WorkoutEntry)

    @Update
    suspend fun update(entry: WorkoutEntry)

    @Delete
    suspend fun delete(entry: WorkoutEntry)

    @Query("DELETE FROM workout_entries WHERE day_id = :dayId")
    suspend fun deleteByDayId(dayId: Int)

    @Query("DELETE FROM workout_entries")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM workout_entries WHERE day_id = :dayId")
    suspend fun getWorkoutEntryCountForDay(dayId: Int): Int

    @Query("SELECT COUNT(*) FROM workout_entries")
    suspend fun getWorkoutEntryCount(): Int

    @Query("SELECT * FROM workout_entries WHERE exercise_id = :exerciseId ORDER BY completedAt DESC")
    suspend fun getWorkoutEntriesForExercise(exerciseId: Int): List<WorkoutEntry>

    /**
     * Get completed workout entries for a specific exercise within a date range
     * Used specifically for the performance screen
     */
    @Query("SELECT * FROM workout_entries WHERE exercise_id = :exerciseId AND isCompleted = 1 AND completedAt IS NOT NULL AND completedAt >= :startTime ORDER BY completedAt DESC")
    suspend fun getCompletedWorkoutEntriesForExercise(exerciseId: Int, startTime: Long): List<WorkoutEntry>
}

data class WorkoutEntryWithExercise(
    val id: Int,
    val dayId: Int,
    val exerciseId: Int,
    val sets: Int,
    val reps: Int,
    val isCompleted: Boolean,
    val exerciseName: String,
    val isCompound: Boolean,
    val totalSecondsSpent: Int = 0, // Time tracking field
    val completedAt: Long? = null // Adding completedAt timestamp
)
