package com.example.offlinepplworkoutapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    val id: Int,
    val name: String,
    val isCompound: Boolean,
    val primaryMuscle: String = "",      // NEW - Main muscle group targeted
    val secondaryMuscles: String = "",   // NEW - Secondary muscles (comma separated)
    val equipment: String = "",          // NEW - Required equipment
    val difficulty: String = "Intermediate", // NEW - Beginner/Intermediate/Advanced
    val instructions: String = "",       // NEW - Step-by-step exercise guide
    val tips: String = "",              // NEW - Form tips and common mistakes
    val category: String = ""           // NEW - Push/Pull/Legs classification
)
