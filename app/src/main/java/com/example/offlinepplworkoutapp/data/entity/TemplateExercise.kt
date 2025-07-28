package com.example.offlinepplworkoutapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/**
 * Template Exercise Junction Entity - Links exercises to workout templates
 *
 * This entity defines which exercises belong to each template and their configuration
 * (sets, reps, rest periods, order). This allows for flexible template composition.
 */
@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val templateId: Int,           // FK to workout_templates
    val exerciseId: Int,           // FK to exercises
    val orderIndex: Int,           // Exercise order in template (0, 1, 2, etc.)
    val sets: Int,                 // Default sets for this exercise in this template
    val reps: Int,                 // Default reps for this exercise
    val restSeconds: Int,          // Recommended rest between sets (in seconds)

    // Optional fields for advanced configurations
    val weight: Double = 0.0,      // Suggested starting weight (if applicable)
    val notes: String = "",        // Exercise-specific notes for this template
    val isSuperset: Boolean = false, // For future superset implementation
    val supersetGroup: Int = 0     // Group ID for supersets (0 = no superset)
)
