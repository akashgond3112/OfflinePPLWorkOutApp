package com.example.offlinepplworkoutapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    val id: Int,
    val name: String,
    val isCompound: Boolean
)
