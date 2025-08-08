package com.example.offlinepplworkoutapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_entries",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutDay::class,
            parentColumns = ["id"],
            childColumns = ["day_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["day_id"]),
        Index(value = ["exercise_id"])
    ]
)
data class WorkoutEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "day_id")
    val dayId: Int,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Int,
    val sets: Int,
    val reps: Int,
    val isCompleted: Boolean = false,
    val totalSecondsSpent: Int = 0, // New field for time tracking
    val completedAt: Long? = null // New timestamp for completion date/time
)
