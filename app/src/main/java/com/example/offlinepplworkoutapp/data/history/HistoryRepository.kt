package com.example.offlinepplworkoutapp.data.history

import com.example.offlinepplworkoutapp.data.dao.SetEntryDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutDayDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.entity.SetEntry
import com.example.offlinepplworkoutapp.data.entity.WorkoutDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for handling workout history data.
 * This provides methods for accessing past workouts and their details.
 */
class HistoryRepository(
    private val workoutDayDao: WorkoutDayDao,
    private val workoutEntryDao: WorkoutEntryDao,
    private val setEntryDao: SetEntryDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Get a list of all workout days, sorted by date (most recent first)
     */
    fun getAllWorkoutDays() = workoutDayDao.getAllWorkoutDays()

    /**
     * Get the most recent workout day
     */
    suspend fun getMostRecentWorkoutDay(): WorkoutDay? {
        val days = workoutDayDao.getAllWorkoutDays().first()
        return days.firstOrNull()
    }

    /**
     * Get a workout day by date
     */
    suspend fun getWorkoutDayByDate(date: String): WorkoutDay? {
        return workoutDayDao.getWorkoutDayByDate(date)
    }

    /**
     * Get workout entries for a specific date
     */
    suspend fun getWorkoutEntriesForDate(date: String): Flow<List<WorkoutEntryWithExercise>> {
        val workoutDay = workoutDayDao.getWorkoutDayByDate(date)
        return workoutDay?.let {
            workoutEntryDao.getWorkoutEntriesForDay(it.id)
        } ?: workoutEntryDao.getWorkoutEntriesForDay(-1) // Return empty flow if no workout found
    }

    /**
     * Get set entries for a workout entry synchronously
     */
    suspend fun getSetsForWorkoutEntrySync(workoutEntryId: Int): List<SetEntry> {
        return setEntryDao.getSetsForWorkoutEntrySync(workoutEntryId)
    }

    /**
     * Get set entries for a workout entry
     */
    fun getSetsForWorkoutEntry(workoutEntryId: Int) = setEntryDao.getSetsForWorkoutEntry(workoutEntryId)

    /**
     * Check if there is a next (more recent) workout after the provided date
     */
    suspend fun hasNextWorkout(currentDate: String): Boolean {
        return getNextWorkoutDate(currentDate) != null
    }

    /**
     * Check if there is a previous (older) workout before the provided date
     */
    suspend fun hasPreviousWorkout(currentDate: String): Boolean {
        return getPreviousWorkoutDate(currentDate) != null
    }

    /**
     * Get the next workout date after the provided date
     * Returns null if this is the most recent workout
     */
    suspend fun getNextWorkoutDate(currentDate: String): String? {
        val allDays = workoutDayDao.getAllWorkoutDays().first()
        val sortedDates = allDays.map { it.date }.sortedDescending()

        val currentIndex = sortedDates.indexOf(currentDate)
        return if (currentIndex > 0) sortedDates[currentIndex - 1] else null
    }

    /**
     * Get the previous workout date before the provided date
     */
    suspend fun getPreviousWorkoutDate(currentDate: String): String? {
        val allDays = workoutDayDao.getAllWorkoutDays().first()
        val sortedDates = allDays.map { it.date }.sortedDescending()

        val currentIndex = sortedDates.indexOf(currentDate)
        return if (currentIndex >= 0 && currentIndex < sortedDates.size - 1) sortedDates[currentIndex + 1] else null
    }

    /**
     * Calculate total time spent on a workout
     */
    suspend fun calculateTotalWorkoutTime(workoutDayId: Int): Int {
        val entries = workoutEntryDao.getWorkoutEntriesForDaySync(workoutDayId)
        return entries.sumOf { it.totalSecondsSpent }
    }

    /**
     * Calculate completed sets vs total sets for a workout day
     */
    suspend fun calculateSetsCompletion(workoutDayId: Int): Pair<Int, Int> {
        val entries = workoutEntryDao.getWorkoutEntriesForDaySync(workoutDayId)
        var completedSets = 0
        var totalSets = 0

        for (entry in entries) {
            val setEntries = setEntryDao.getSetsForWorkoutEntrySync(entry.id)
            completedSets += setEntries.count { it.isCompleted }
            totalSets += setEntries.size
        }

        return Pair(completedSets, totalSets)
    }
}
