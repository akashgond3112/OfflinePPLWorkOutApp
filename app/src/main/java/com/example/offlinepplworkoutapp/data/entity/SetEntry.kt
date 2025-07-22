package com.example.offlinepplworkoutapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "set_entries",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntry::class,
            parentColumns = ["id"],
            childColumns = ["workout_entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workout_entry_id"])
    ]
)
data class SetEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "workout_entry_id")
    val workoutEntryId: Int,
    val setNumber: Int, // 1, 2, 3, 4...
    val isCompleted: Boolean = false,
    val elapsedTimeSeconds: Int = 0, // Time spent on this specific set
    val completedAt: Long? = null // Timestamp when set was completed
)
