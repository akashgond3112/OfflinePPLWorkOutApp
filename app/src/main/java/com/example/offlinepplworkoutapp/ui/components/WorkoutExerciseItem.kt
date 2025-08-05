package com.example.offlinepplworkoutapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository

@Composable
fun WorkoutExerciseItemWithSetProgress(
    workoutEntry: WorkoutEntryWithExercise,
    onClick: (WorkoutEntryWithExercise) -> Unit = {},
    repository: WorkoutRepository
) {
    // Get the actual completed sets count from the database for this specific exercise
    var completedSetsCount by remember { mutableStateOf(0) }

    // Load the actual completed sets for this specific workout entry
    LaunchedEffect(workoutEntry.id) {
        completedSetsCount = repository.getCompletedSetsCount(workoutEntry.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(workoutEntry) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workoutEntry.exerciseName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (workoutEntry.isCompound) "Compound Exercise" else "Isolation Exercise",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (workoutEntry.isCompound)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                }

                // Sets and Reps display
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${workoutEntry.sets} × ${workoutEntry.reps} reps",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Set Progress Bar and Status - NOW USING ACTUAL COMPLETED SETS FROM DATABASE
            val progress =
                if (workoutEntry.sets > 0) completedSetsCount.toFloat() / workoutEntry.sets.toFloat() else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (workoutEntry.isCompleted) "✅ Completed" else "Set $completedSetsCount/${workoutEntry.sets}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (workoutEntry.isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (workoutEntry.isCompleted) FontWeight.Bold else FontWeight.Normal
                    )

                    if (!workoutEntry.isCompleted) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Total time spent display
                if (workoutEntry.totalSecondsSpent > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Time Spent",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${workoutEntry.totalSecondsSpent / 60}:${
                                String.format(
                                    "%02d",
                                    workoutEntry.totalSecondsSpent % 60
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}