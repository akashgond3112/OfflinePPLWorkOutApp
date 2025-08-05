package com.example.offlinepplworkoutapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.components.WorkoutExerciseItemWithSetProgress
import com.example.offlinepplworkoutapp.ui.components.WorkoutProgressIndicator
import com.example.offlinepplworkoutapp.ui.viewmodel.DailyWorkoutViewModel

@Composable
fun DailyWorkoutScreen(
    viewModel: DailyWorkoutViewModel,
    repository: WorkoutRepository,
    modifier: Modifier = Modifier,
    onExerciseClick: (WorkoutEntryWithExercise) -> Unit = {},
    onTemplateSelectionClick: () -> Unit = {}
) {

    val todaysWorkout by viewModel.todaysWorkout.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val completionProgress by viewModel.completionProgress.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    var showResetWarning by remember { mutableStateOf(false) }
    var exerciseToReset by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with day, workout type, and timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${viewModel.getCurrentDayName()}'s Workout",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = viewModel.getWorkoutTypeName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Timer Display in top-right corner
            if (isTimerRunning) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Timer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = viewModel.formatTime(timerSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Progress indicator for non-rest days
        if (todaysWorkout.isNotEmpty()) {
            WorkoutProgressIndicator(
                progress = completionProgress,
                completionPercentage = viewModel.getCompletionPercentage(),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading today's workout...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else if (todaysWorkout.isEmpty()) {
            // Check if it's a rest day or if workout needs to be created
            if (viewModel.getWorkoutTypeName() == "Rest Day") {
                // Rest Day with Background Image
                RestDayScreen()
            } else {
                // Show "Start Workout" screen after reset or for new day
                StartWorkoutScreen(
                    workoutType = viewModel.getWorkoutTypeName(),
                    onStartWorkout = {
                        println("🔥 UI: Start Workout button clicked!")
                        viewModel.createTodaysWorkout()
                    },
                    onTemplateSelection = onTemplateSelectionClick
                )
            }
        } else {
            // Exercise list header with template selection option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercises: ${todaysWorkout.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Template selection button
                OutlinedButton(
                    onClick = onTemplateSelectionClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Change Template",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Change Template",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            LazyColumn {
                items(todaysWorkout) { workoutEntry ->
                    WorkoutExerciseItemWithSetProgress(
                        workoutEntry = workoutEntry,
                        onClick = onExerciseClick,
                        repository = repository
                    )
                }
            }
        }
    }

    // Auto-save logic when all exercises are completed
    if (completionProgress == 1.0f) {
        viewModel.saveTotalTimeSpent(timerSeconds)
    }

    // Reset Protection Dialog
    if (showResetWarning && exerciseToReset != null) {
        AlertDialog(
            onDismissRequest = {
                showResetWarning = false
                exerciseToReset = null
            },
            title = {
                Text(
                    text = "⚠️ Reset Exercise",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will reset your entire workout progress for this exercise. It's better to go to the exercise detail to reset specific reps.\n\nAre you sure you want to completely reset this exercise?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        exerciseToReset?.let { exerciseId ->
                            val exercise = todaysWorkout.find { it.id == exerciseId }
                            exercise?.let { onExerciseClick(it) }
                        }
                        showResetWarning = false
                        exerciseToReset = null
                    }
                ) {
                    Text("Go to Details")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetWarning = false
                        exerciseToReset = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
