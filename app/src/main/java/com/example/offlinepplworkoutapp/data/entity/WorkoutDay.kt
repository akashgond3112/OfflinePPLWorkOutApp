package com.example.offlinepplworkoutapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_days")
data class WorkoutDay(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String // ISO YYYY-MM-DD format
)
