package com.example.offlinepplworkoutapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.viewmodel.ExerciseDetailViewModel
import com.example.offlinepplworkoutapp.ui.viewmodel.ExerciseDetailViewModelFactory
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    workoutEntry: WorkoutEntryWithExercise,
    repository: WorkoutRepository,
    onBackClick: () -> Unit,
    onSaveChanges: (sets: Int, reps: Int, isCompleted: Boolean) -> Unit
) {
    val viewModel: ExerciseDetailViewModel = viewModel(
        factory = ExerciseDetailViewModelFactory(workoutEntry, repository)
    )

    val setTimers by viewModel.setTimers.collectAsState()
    val currentRunningSet by viewModel.currentRunningSet.collectAsState()
    val totalExerciseTime by viewModel.totalExerciseTime.collectAsState()
    val completedSets by viewModel.completedSets.collectAsState()
    val isExerciseCompleted by viewModel.isExerciseCompleted.collectAsState()

    // Only update the completion status when explicitly going back,
    // not immediately when the screen opens
    val originalCompletionStatus = remember { workoutEntry.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = workoutEntry.exerciseName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sets: $completedSets/${workoutEntry.sets} • Total: ${formatTime(totalExerciseTime)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Only update exercise completion status when explicitly navigating back,
                        // and only if status has actually changed
                        val shouldMarkCompleted = isExerciseCompleted && completedSets == workoutEntry.sets
                        if (shouldMarkCompleted != originalCompletionStatus) {
                            onSaveChanges(workoutEntry.sets, workoutEntry.reps, shouldMarkCompleted)
                        }
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exercise completion status
            item {
                if (isExerciseCompleted) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exercise Completed! 🎉",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Set cards
            itemsIndexed(setTimers) { index, setTimer ->
                val activeSetIndex by viewModel.activeSetIndex.collectAsState()
                SetTimerCard(
                    setNumber = index + 1,
                    totalSets = workoutEntry.sets,
                    targetReps = workoutEntry.reps,
                    setTimer = setTimer.elapsedTime,
                    isCurrentSet = currentRunningSet == index,
                    isCompleted = setTimer.isCompleted,
                    isActive = index == activeSetIndex,  // Pass active set index
                    onStartTimer = { viewModel.startSetTimer(index) },
                    onStopTimer = { viewModel.stopSetTimer(index) },
                    onCompleteSet = { viewModel.completeSet(index) }
                )
            }
        }
    }
}

@Composable
fun SetTimerCard(
    setNumber: Int,
    totalSets: Int,
    targetReps: Int,
    setTimer: Long,
    isCurrentSet: Boolean,
    isCompleted: Boolean = false,
    isActive: Boolean = false,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onCompleteSet: () -> Unit
) {
    val backgroundColor = when {
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
        isCurrentSet -> MaterialTheme.colorScheme.primaryContainer
        isActive -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) // Dimmed for inactive sets
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isActive || isCurrentSet) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Set Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Set $setNumber of $totalSets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!isActive && !isCompleted && !isCurrentSet)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    // Show completion indicator
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Timer Text
                Text(
                    text = formatTime(setTimer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Target reps display
            Text(
                text = "Target: $targetReps reps",
                style = MaterialTheme.typography.bodyMedium,
                color = if (!isActive && !isCompleted && !isCurrentSet)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Single action button - changes between Start and Stop
            if (isCompleted) {
                // Show completed message for completed sets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (isActive || isCurrentSet) {
                Button(
                    onClick = if (isCurrentSet) onStopTimer else onStartTimer,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentSet)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isCurrentSet)
                            Icons.Default.Close
                        else
                            Icons.Default.PlayArrow,
                        contentDescription = if (isCurrentSet) "Stop" else "Start",
                        tint = if (isCurrentSet)
                            MaterialTheme.colorScheme.onError
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCurrentSet) "Stop Set" else "Start Set",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Disabled button for inactive sets
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false
                ) {
                    Text(
                        text = "Waiting...",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60)) % 24

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
