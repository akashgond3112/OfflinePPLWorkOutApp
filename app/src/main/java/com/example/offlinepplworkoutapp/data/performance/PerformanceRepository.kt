package com.example.offlinepplworkoutapp.data.performance

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.entity.Exercise
import com.example.offlinepplworkoutapp.data.entity.SetEntry
import com.example.offlinepplworkoutapp.data.entity.WorkoutEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for handling performance analytics data for exercises
 */
class PerformanceRepository(private val database: PPLWorkoutDatabase) {

    /**
     * Get performance data for all exercises within the specified time period
     * @param days Number of days to look back (7, 14, or 30)
     * @return Flow of a list of exercise performance data
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllExercisesPerformance(days: Int): Flow<List<ExercisePerformance>> = flow {
        // Calculate the date from days ago, but we want to include the entire current day
        // so we use (days - 1) days ago, which effectively includes today
        val startTime = LocalDate.now().minusDays((days - 1).toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val startTimeFormatted = dateFormat.format(Date(startTime))
        Log.d("PerformanceRepo", "🔍 Loading performance data for the last $days days (from $startTimeFormatted)")

        try {
            // Get exercises as a List<Exercise> by collecting the first value from the Flow
            val exercises = database.exerciseDao().getAllExercises().first()
            Log.d("PerformanceRepo", "🔍 Found ${exercises.size} exercises in database")

            // Build performance data for each exercise
            val performanceList = exercises.map { exercise ->
                try {
                    Log.d("PerformanceRepo", "🔍 Building performance for exercise: ${exercise.id} - ${exercise.name}")
                    val performance = buildExercisePerformance(exercise, startTime)
                    Log.d("PerformanceRepo", "✅ Performance for ${exercise.name}: " +
                            "MaxWeight=${performance.maxWeight}kg, " +
                            "MaxReps=${performance.maxReps}, " +
                            "Sessions=${performance.sessionsCount}, " +
                            "DataPoints=${performance.progressData.size}"
                    )
                    performance
                } catch (e: Exception) {
                    Log.e("PerformanceRepo", "❌ Error building performance for ${exercise.name}: ${e.message}")
                    // If processing a specific exercise fails, return a placeholder with error state
                    // This prevents one bad exercise from breaking the entire performance tab
                    ExercisePerformance(
                        exercise = exercise,
                        progressData = emptyList(),
                        maxWeight = 0f,
                        maxReps = 0,
                        volumeProgress = 0f,
                        weightProgress = 0f,
                        sessionsCount = 0,
                        hasError = true
                    )
                }
            }

            emit(performanceList)
        } catch (e: Exception) {
            Log.e("PerformanceRepo", "�� Error loading exercise performance: ${e.message}")
            // In case of error, emit an empty list
            emit(emptyList<ExercisePerformance>())
        }
    }

    /**
     * Build performance data for a single exercise
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun buildExercisePerformance(
        exercise: Exercise,
        startTime: Long
    ): ExercisePerformance {
        // Find workout entries for this exercise within the time range
        val workoutEntries = getWorkoutEntriesForExercise(exercise.id, startTime)

        if (workoutEntries.isEmpty()) {
            // No workout data found for this exercise in the given time range
            return ExercisePerformance(
                exercise = exercise,
                progressData = emptyList(),
                maxWeight = 0f,
                maxReps = 0,
                volumeProgress = 0f,
                weightProgress = 0f,
                sessionsCount = 0
            )
        }

        // Get all set entries for these workout entries
        val setEntries = getAllSetsForWorkoutEntries(workoutEntries)

        // Calculate performance metrics
        val progressPoints = calculateProgressPoints(setEntries)
        val maxWeight = calculateMaxWeight(setEntries)
        val maxReps = calculateMaxReps(setEntries)
        val volumeProgress = calculateVolumeProgress(setEntries)
        val weightProgress = calculateWeightProgress(setEntries)

        return ExercisePerformance(
            exercise = exercise,
            progressData = progressPoints,
            maxWeight = maxWeight,
            maxReps = maxReps,
            volumeProgress = volumeProgress,
            weightProgress = weightProgress,
            sessionsCount = workoutEntries.size
        )
    }

    /**
     * Get workout entries for a specific exercise within a date range
     */
    private suspend fun getWorkoutEntriesForExercise(exerciseId: Int, startTime: Long): List<WorkoutEntry> {
        // Use the new specialized query for performance data that only gets completed workouts
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        Log.d("PerformanceRepo", "🔍 Fetching completed workout entries for exercise ID: $exerciseId from ${dateFormat.format(Date(startTime))}")

        val filteredEntries = database.workoutEntryDao().getCompletedWorkoutEntriesForExercise(exerciseId, startTime)

        Log.d("PerformanceRepo", "🔍 Found ${filteredEntries.size} completed entries")

        filteredEntries.forEach { entry ->
            entry.completedAt?.let { timestamp ->
                Log.d("PerformanceRepo", "📝 Completed workout entry: ID=${entry.id}, " +
                        "ExerciseID=${entry.exerciseId}, " +
                        "CompletedAt=${dateFormat.format(Date(timestamp))}")
            }
        }

        return filteredEntries
    }

    /**
     * Get all set entries for a list of workout entries
     */
    private suspend fun getAllSetsForWorkoutEntries(workoutEntries: List<WorkoutEntry>): List<SetEntry> {
        val allSets = mutableListOf<SetEntry>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        workoutEntries.forEach { workoutEntry ->
            val sets = database.setEntryDao().getSetsForWorkoutEntrySync(workoutEntry.id)
            Log.d("PerformanceRepo", "🔍 Found ${sets.size} sets for workout entry ID: ${workoutEntry.id}")

            val completedSets = sets.filter { it.isCompleted }
            Log.d("PerformanceRepo", "🔍 Completed sets: ${completedSets.size} out of ${sets.size}")

            completedSets.forEach { set ->
                Log.d("PerformanceRepo", "📝 Set: ID=${set.id}, " +
                        "Weight=${set.weightUsed}kg, " +
                        "Reps=${set.repsPerformed}, " +
                        "CompletedAt=${set.completedAt?.let { dateFormat.format(Date(it)) } ?: "null"}")
            }

            allSets.addAll(completedSets)
        }

        return allSets
    }

    /**
     * Calculate progress points for graphing
     */
    private fun calculateProgressPoints(setEntries: List<SetEntry>): List<ProgressPoint> {
        // Group sets by date (day)
        val groupedByDate = setEntries
            .filter { it.completedAt != null && it.repsPerformed > 0 && it.weightUsed > 0 }
            .groupBy {
                it.completedAt!! / (24 * 60 * 60 * 1000) // Convert timestamp to day
            }

        // For each day, create a progress point with best performance
        return groupedByDate.map { (_, sets) ->
            // Find the set with the highest volume (weight * reps)
            val bestSet = sets.maxByOrNull { it.weightUsed * it.repsPerformed } ?: sets.first()

            ProgressPoint(
                date = bestSet.completedAt!!,
                weight = bestSet.weightUsed,
                reps = bestSet.repsPerformed,
                volume = bestSet.weightUsed * bestSet.repsPerformed
            )
        }.sortedBy { it.date }
    }

    /**
     * Calculate the maximum weight used for the exercise
     */
    private fun calculateMaxWeight(setEntries: List<SetEntry>): Float {
        return setEntries
            .filter { it.isCompleted && it.repsPerformed > 0 }
            .maxOfOrNull { it.weightUsed } ?: 0f
    }

    /**
     * Calculate the maximum reps performed for the exercise
     */
    private fun calculateMaxReps(setEntries: List<SetEntry>): Int {
        return setEntries
            .filter { it.isCompleted && it.weightUsed > 0 }
            .maxOfOrNull { it.repsPerformed } ?: 0
    }

    /**
     * Calculate volume progress (percentage change)
     * Using improved calculation that better handles varying amounts of data
     */
    private fun calculateVolumeProgress(setEntries: List<SetEntry>): Float {
        // Filter valid entries
        val validEntries = setEntries
            .filter { it.completedAt != null && it.isCompleted && it.repsPerformed > 0 && it.weightUsed > 0 }
            .sortedBy { it.completedAt }

        // Need at least 2 data points for a trend
        if (validEntries.size < 2) return 0f

        // Determine sampling approach based on data quantity
        return when {
            // For a small dataset (2-5 points), compare first vs last
            validEntries.size <= 5 -> {
                val firstVolume = validEntries.first().weightUsed * validEntries.first().repsPerformed
                val lastVolume = validEntries.last().weightUsed * validEntries.last().repsPerformed

                if (firstVolume > 0) {
                    ((lastVolume - firstVolume) / firstVolume * 100)
                } else 0f
            }

            // For larger datasets, use a more robust approach with averages
            else -> {
                // Use 30% of data points or at least 2 points for the samples
                val sampleSize = (validEntries.size * 0.3).toInt().coerceAtLeast(2)

                // Calculate average volume at the beginning of the period
                val firstSegment = validEntries.take(sampleSize)
                val firstAvgVolume = firstSegment
                    .map { it.weightUsed * it.repsPerformed }
                    .average()

                // Calculate average volume at the end of the period
                val lastSegment = validEntries.takeLast(sampleSize)
                val lastAvgVolume = lastSegment
                    .map { it.weightUsed * it.repsPerformed }
                    .average()

                // Calculate percentage change
                if (firstAvgVolume > 0) {
                    ((lastAvgVolume - firstAvgVolume) / firstAvgVolume * 100).toFloat()
                } else 0f
            }
        }
    }

    /**
     * Calculate weight progress (percentage change)
     * Using improved calculation that better handles varying amounts of data
     */
    private fun calculateWeightProgress(setEntries: List<SetEntry>): Float {
        // Filter valid entries
        val validEntries = setEntries
            .filter { it.completedAt != null && it.isCompleted && it.repsPerformed > 0 && it.weightUsed > 0 }
            .sortedBy { it.completedAt }

        // Need at least 2 data points for a trend
        if (validEntries.size < 2) return 0f

        // Determine sampling approach based on data quantity
        return when {
            // For a small dataset (2-5 points), compare first vs last
            validEntries.size <= 5 -> {
                val firstWeight = validEntries.first().weightUsed
                val lastWeight = validEntries.last().weightUsed

                if (firstWeight > 0) {
                    ((lastWeight - firstWeight) / firstWeight * 100)
                } else 0f
            }

            // For larger datasets, use a more robust approach with averages
            else -> {
                // Use 30% of data points or at least 2 points for the samples
                val sampleSize = (validEntries.size * 0.3).toInt().coerceAtLeast(2)

                // Calculate average weight at the beginning of the period
                val firstSegment = validEntries.take(sampleSize)
                val firstAvgWeight = firstSegment
                    .map { it.weightUsed }
                    .average()

                // Calculate average weight at the end of the period
                val lastSegment = validEntries.takeLast(sampleSize)
                val lastAvgWeight = lastSegment
                    .map { it.weightUsed }
                    .average()

                // Calculate percentage change
                if (firstAvgWeight > 0) {
                    ((lastAvgWeight - firstAvgWeight) / firstAvgWeight * 100).toFloat()
                } else 0f
            }
        }
    }
}
