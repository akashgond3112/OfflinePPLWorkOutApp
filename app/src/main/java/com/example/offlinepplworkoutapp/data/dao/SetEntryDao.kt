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

    @Query("DELETE FROM set_entries WHERE workout_entry_id = :workoutEntryId")
    suspend fun deleteByWorkoutEntryId(workoutEntryId: Int)

    @Query("DELETE FROM set_entries")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM set_entries WHERE workout_entry_id = :workoutEntryId AND isCompleted = 1")
    suspend fun getCompletedSetsCount(workoutEntryId: Int): Int

    @Query("SELECT COUNT(*) FROM set_entries WHERE workout_entry_id = :workoutEntryId")
    suspend fun getTotalSetsCount(workoutEntryId: Int): Int
}
