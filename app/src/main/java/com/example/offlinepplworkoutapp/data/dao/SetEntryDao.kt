package com.example.offlinepplworkoutapp.data.dao

import androidx.room.*
import com.example.offlinepplworkoutapp.data.entity.SetEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SetEntryDao {

    @Query("SELECT * FROM set_entries WHERE workout_entry_id = :workoutEntryId ORDER BY setNumber")
    fun getSetsForWorkoutEntry(workoutEntryId: Int): Flow<List<SetEntry>>

    @Query("SELECT * FROM set_entries WHERE workout_entry_id = :workoutEntryId ORDER BY setNumber")
    suspend fun getSetsForWorkoutEntrySync(workoutEntryId: Int): List<SetEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<SetEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setEntry: SetEntry)

    @Update
    suspend fun update(setEntry: SetEntry)

    @Query("UPDATE set_entries SET isCompleted = :isCompleted, elapsedTimeSeconds = :elapsedTimeSeconds, completedAt = :completedAt WHERE id = :setId")
    suspend fun updateSetProgress(setId: Int, isCompleted: Boolean, elapsedTimeSeconds: Int, completedAt: Long?)

    // 🚀 NEW: Phase 2.1.1 - Methods for handling set performance data
    @Query("UPDATE set_entries SET isCompleted = :isCompleted, elapsedTimeSeconds = :elapsedTimeSeconds, completedAt = :completedAt, reps_performed = :repsPerformed, weight_used = :weightUsed WHERE id = :setId")
    suspend fun updateSetProgressWithPerformanceData(
        setId: Int,
        isCompleted: Boolean,
        elapsedTimeSeconds: Int,
        completedAt: Long?,
        repsPerformed: Int?,
        weightUsed: Float?
    )

    @Query("UPDATE set_entries SET reps_performed = :repsPerformed, weight_used = :weightUsed WHERE id = :setId")
    suspend fun updateSetPerformanceData(setId: Int, repsPerformed: Int?, weightUsed: Float?)

    @Query("SELECT * FROM set_entries WHERE id = :setId")
    suspend fun getSetById(setId: Int): SetEntry?

    // 🆕 NEW: 2.2.1 - Get set by workout entry and set number for editing functionality
    @Query("SELECT * FROM set_entries WHERE workout_entry_id = :workoutEntryId AND setNumber = :setNumber")
    fun getSetByWorkoutEntryAndSetNumber(workoutEntryId: Int, setNumber: Int): Flow<SetEntry?>

    @Query("DELETE FROM set_entries WHERE workout_entry_id = :workoutEntryId")
    suspend fun deleteByWorkoutEntryId(workoutEntryId: Int)

    @Query("DELETE FROM set_entries")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM set_entries")
    suspend fun getSetEntryCount(): Int

    @Query("SELECT COUNT(*) FROM set_entries WHERE workout_entry_id = :workoutEntryId AND isCompleted = 1")
    suspend fun getCompletedSetsCount(workoutEntryId: Int): Int

    @Query("SELECT COUNT(*) FROM set_entries WHERE workout_entry_id = :workoutEntryId")
    suspend fun getTotalSetsCount(workoutEntryId: Int): Int

    // 🆕 NEW: 2.2.2 - Dynamic Set Management Methods
    @Delete
    suspend fun deleteSet(setEntry: SetEntry)

    @Query("DELETE FROM set_entries WHERE id = :setId")
    suspend fun deleteSetById(setId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(setEntry: SetEntry): Long
}
