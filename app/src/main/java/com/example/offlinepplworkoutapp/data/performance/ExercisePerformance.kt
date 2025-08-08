package com.example.offlinepplworkoutapp.data.performance

import com.example.offlinepplworkoutapp.data.entity.Exercise

/**
 * Data class that represents the performance metrics for a single exercise
 * This is used to display analytics in the Performance tab
 */
data class ExercisePerformance(
    val exercise: Exercise,
    val progressData: List<ProgressPoint>,
    val maxWeight: Float,
    val maxReps: Int,
    val volumeProgress: Float, // Percentage change in total volume
    val weightProgress: Float, // Percentage change in max weight
    val sessionsCount: Int, // Number of sessions this exercise was performed
    val hasError: Boolean = false // Indicates if there was an error processing this exercise's data
)

/**
 * Represents a single data point for graphing exercise progress
 */
data class ProgressPoint(
    val date: Long, // Timestamp
    val weight: Float,
    val reps: Int,
    val volume: Float // weight * reps
)
