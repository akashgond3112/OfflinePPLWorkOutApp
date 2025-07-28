package com.example.offlinepplworkoutapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Workout Template Entity - Defines reusable workout structures
 *
 * This replaces the hardcoded day-based workout logic with a flexible template system.
 * Templates can be customized, shared, and used to create consistent workout experiences.
 */
@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,              // "Push Day 1", "Pull Day 1", "Legs Day 1", etc.
    val description: String,       // "Chest and Triceps focused", "Back and Biceps", etc.
    val estimatedDuration: Int,    // Estimated workout duration in minutes
    val difficulty: String,        // "Beginner", "Intermediate", "Advanced"
    val category: String,          // "Push", "Pull", "Legs"

    // Optional fields for future enhancements
    val isCustom: Boolean = false, // User-created vs predefined templates
    val isActive: Boolean = true,  // Can be deactivated without deletion
    val createdDate: String = "",  // When template was created
    val lastUsedDate: String = ""  // Track template usage
)
