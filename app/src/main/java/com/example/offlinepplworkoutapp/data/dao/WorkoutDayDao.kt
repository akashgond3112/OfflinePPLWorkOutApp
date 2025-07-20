package com.example.offlinepplworkoutapp.data.dao

import androidx.room.*
import com.example.offlinepplworkoutapp.data.entity.WorkoutDay
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDayDao {

    @Query("SELECT * FROM workout_days WHERE date = :date")
    suspend fun getWorkoutDayByDate(date: String): WorkoutDay?

    @Query("SELECT * FROM workout_days ORDER BY date DESC")
    fun getAllWorkoutDays(): Flow<List<WorkoutDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workoutDay: WorkoutDay): Long

    @Delete
    suspend fun delete(workoutDay: WorkoutDay)

    @Query("DELETE FROM workout_days WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)

    @Query("DELETE FROM workout_days")
    suspend fun deleteAll()
}
