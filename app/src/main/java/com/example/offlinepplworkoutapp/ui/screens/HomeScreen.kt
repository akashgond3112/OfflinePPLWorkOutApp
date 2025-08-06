package com.example.offlinepplworkoutapp.ui.screens

import androidx.compose.runtime.Composable
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository

@Composable
fun HomeScreen(repository: WorkoutRepository) {
    // This simply wraps the existing MainScreen
    MainScreen(repository = repository)
}
