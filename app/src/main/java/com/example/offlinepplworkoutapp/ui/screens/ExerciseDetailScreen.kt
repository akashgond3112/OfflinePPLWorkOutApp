package com.example.offlinepplworkoutapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    workoutEntry: WorkoutEntryWithExercise,
    onBackClick: () -> Unit,
    onSaveChanges: (sets: Int, reps: Int, isCompleted: Boolean) -> Unit
) {
    var sets by remember { mutableIntStateOf(workoutEntry.sets) }
    var reps by remember { mutableIntStateOf(workoutEntry.reps) }
    var isCompleted by remember { mutableStateOf(workoutEntry.isCompleted) }
    var hasChanges by remember { mutableStateOf(false) }

    // Track if values have changed
    LaunchedEffect(sets, reps, isCompleted) {
        hasChanges = sets != workoutEntry.sets ||
                    reps != workoutEntry.reps ||
                    isCompleted != workoutEntry.isCompleted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = workoutEntry.exerciseName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (hasChanges) {
                FloatingActionButton(
                    onClick = {
                        onSaveChanges(sets, reps, isCompleted)
                        onBackClick()
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Save Changes"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Exercise Type Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (workoutEntry.isCompound)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = if (workoutEntry.isCompound) "🏋️ COMPOUND EXERCISE" else "🎯 ISOLATION EXERCISE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = if (workoutEntry.isCompound)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Completion Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exercise Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "Completed" else "Start",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isCompleted)
                                MaterialTheme.colorScheme.primary
                            else
                                Color(0xFF4CAF50) // Green color for "Start"
                        )

                        IconButton(
                            onClick = { isCompleted = !isCompleted }
                        ) {
                            Icon(
                                imageVector = if (isCompleted)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.PlayArrow,
                                contentDescription = if (isCompleted) "Mark as Start" else "Mark as Completed",
                                tint = if (isCompleted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color(0xFF4CAF50) // Green color for start state
                            )
                        }
                    }
                }
            }

            // Sets and Reps Editor
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Exercise Parameters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sets Input
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sets",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = sets.toString(),
                                onValueChange = { newValue ->
                                    newValue.toIntOrNull()?.let { validSets ->
                                        if (validSets in 1..10) {
                                            sets = validSets
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Reps Input
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reps",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = reps.toString(),
                                onValueChange = { newValue ->
                                    newValue.toIntOrNull()?.let { validReps ->
                                        if (validReps in 1..50) {
                                            reps = validReps
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Preview Text
                    Text(
                        text = "$sets × $reps reps",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Exercise Tips (if compound)
            if (workoutEntry.isCompound) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 Compound Exercise Tips",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )

                        Text(
                            text = "• Focus on proper form over heavy weight\n• Allow longer rest periods (90-120s)\n• Engages multiple muscle groups\n• Great for building strength and mass",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            if (hasChanges) {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}
