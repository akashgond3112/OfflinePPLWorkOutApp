package com.example.offlinepplworkoutapp.ui.screens.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.ui.components.history.HistoryDateNavigator
import com.example.offlinepplworkoutapp.ui.components.history.HistoryExerciseItem
import com.example.offlinepplworkoutapp.ui.components.history.WorkoutSummaryStats
import com.example.offlinepplworkoutapp.ui.viewmodel.history.HistoryViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    database: PPLWorkoutDatabase,
    onBackClicked: () -> Unit,
    onExerciseClicked: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(database)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val exerciseSets by viewModel.selectedExerciseSets.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout History") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                // Loading state
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                // Error state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { viewModel.loadMostRecentWorkout() }) {
                        Text("Retry")
                    }
                }
            } else {
                // Content state
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Date selector
                    item {
                        HistoryDateNavigator(
                            currentDate = uiState.currentDate,
                            hasPreviousWorkout = uiState.hasPreviousWorkout,
                            hasNextWorkout = uiState.hasNextWorkout,
                            onPreviousClicked = { viewModel.loadPreviousWorkout() },
                            onNextClicked = { viewModel.loadNextWorkout() }
                        )
                    }

                    // Workout summary
                    item {
                        WorkoutSummaryStats(
                            workoutType = uiState.workoutType,
                            formattedTime = viewModel.formatWorkoutTime(uiState.totalWorkoutTime),
                            completedSets = uiState.completedSets,
                            totalSets = uiState.totalSets
                        )
                    }

                    // Exercises list
                    if (uiState.exercises.isEmpty()) {
                        item {
                            Text(
                                text = "No exercises found for this workout",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            )
                        }
                    } else {
                        items(uiState.exercises) { exercise ->
                            HistoryExerciseItem(
                                exercise = exercise,
                                sets = exerciseSets[exercise.id] ?: emptyList(),
                                totalTimeFormatted = viewModel.formatWorkoutTime(exercise.totalSecondsSpent),
                            )
                        }
                    }
                }
            }
        }
    }
}
