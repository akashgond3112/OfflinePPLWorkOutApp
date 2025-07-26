package com.example.offlinepplworkoutapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.theme.*
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
    // 🔧 FIX: Add unique key to force new ViewModel creation for each exercise
    val viewModel: ExerciseDetailViewModel = viewModel(
        key = "exercise_${workoutEntry.id}", // Unique key for each exercise
        factory = ExerciseDetailViewModelFactory(workoutEntry, repository)
    )

    println("🔧 UI: Created ExerciseDetailViewModel for ${workoutEntry.exerciseName} (ID: ${workoutEntry.id})")

    val setTimers by viewModel.setTimers.collectAsState()
    val currentRunningSet by viewModel.currentRunningSet.collectAsState()
    val totalExerciseTime by viewModel.totalExerciseTime.collectAsState()
    val completedSets by viewModel.completedSets.collectAsState()
    val isExerciseCompleted by viewModel.isExerciseCompleted.collectAsState()

    val originalCompletionStatus = remember { workoutEntry.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = workoutEntry.exerciseName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCoral
                            )
                        )
                        Text(
                            text = "Sets: $completedSets/${workoutEntry.sets} • Total: ${formatTime(totalExerciseTime)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val shouldMarkCompleted = isExerciseCompleted && completedSets == workoutEntry.sets
                        if (shouldMarkCompleted != originalCompletionStatus) {
                            onSaveChanges(workoutEntry.sets, workoutEntry.reps, shouldMarkCompleted)
                        }
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryCoral
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise completion status with animation
            item {
                AnimatedVisibility(
                    visible = isExerciseCompleted,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    CompletionCard()
                }
            }

            // Set cards with improved design
            itemsIndexed(setTimers) { index, setTimer ->
                val activeSetIndex by viewModel.activeSetIndex.collectAsState()
                ModernSetTimerCard(
                    setNumber = index + 1,
                    totalSets = workoutEntry.sets,
                    targetReps = workoutEntry.reps,
                    setTimer = setTimer.elapsedTime,
                    isCurrentSet = currentRunningSet == index,
                    isCompleted = setTimer.isCompleted,
                    isActive = index == activeSetIndex,
                    isLocked = index > activeSetIndex && !setTimer.isCompleted,
                    onStartTimer = { viewModel.startSetTimer(index) },
                    onStopTimer = { viewModel.stopSetTimer(index) },
                    onCompleteSet = { viewModel.completeSet(index) }
                )
            }
        }
    }
}

@Composable
fun CompletionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(SuccessGreen, TealSecondary)
                )
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = TextOnPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Exercise Completed! 🎉",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
            )
        }
    }
}

@Composable
fun ModernSetTimerCard(
    setNumber: Int,
    totalSets: Int,
    targetReps: Int,
    setTimer: Long,
    isCurrentSet: Boolean,
    isCompleted: Boolean = false,
    isActive: Boolean = false,
    isLocked: Boolean = false,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onCompleteSet: () -> Unit
) {
    // Animation for card state changes
    val animatedElevation by animateDpAsState(
        targetValue = when {
            isCurrentSet -> 12.dp
            isActive -> 6.dp
            else -> 2.dp
        },
        animationSpec = tween(300)
    )

    // Card colors based on state
    val cardColors = when {
        isCompleted -> CardDefaults.cardColors(
            containerColor = SuccessGreen.copy(alpha = 0.1f)
        )
        isCurrentSet -> CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
        isActive -> CardDefaults.cardColors(
            containerColor = AmberAccent.copy(alpha = 0.1f)
        )
        isLocked -> CardDefaults.cardColors(
            containerColor = TextSecondary.copy(alpha = 0.05f)
        )
        else -> CardDefaults.cardColors(
            containerColor = CardBackground
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(animatedElevation, RoundedCornerShape(16.dp))
            .then(
                if (isCurrentSet) {
                    Modifier
                        .border(
                            width = 3.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(ProgressStart, ProgressEnd)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ProgressStart.copy(alpha = 0.1f),
                                    ProgressEnd.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                } else {
                    Modifier
                }
            ),
        colors = cardColors,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Set Header with improved typography
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Set status icon
                    when {
                        isCompleted -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        isCurrentSet -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Active",
                                tint = PrimaryCoral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        isLocked -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Ready",
                                tint = TealSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Set $setNumber of $totalSets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isLocked -> TextSecondary.copy(alpha = 0.6f)
                                else -> TextPrimary
                            }
                        )
                    )
                }

                // Timer display with improved styling
                TimerDisplay(
                    time = setTimer,
                    isActive = isCurrentSet,
                    isCompleted = isCompleted
                )
            }

            // Target reps info
            Text(
                text = "Target: $targetReps reps",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            )

            // Action buttons with modern design
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    isCompleted -> {
                        // Completed state - show success message
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Set Completed",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    isLocked -> {
                        // Locked state
                        Text(
                            text = "Complete previous sets first",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    isCurrentSet -> {
                        // Active set - show stop button
                        Button(
                            onClick = onStopTimer,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Set")
                        }

                        Button(
                            onClick = onCompleteSet,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Complete",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark as Done")
                        }
                    }
                    else -> {
                        // Ready to start
                        Button(
                            onClick = onStartTimer,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCoral
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLocked
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Set")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerDisplay(
    time: Long,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val textColor = when {
        isCompleted -> SuccessGreen
        isActive -> PrimaryCoral
        else -> TextSecondary
    }

    Text(
        text = formatTime(time),
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    )
}

private fun formatTime(timeInSeconds: Long): String {
    val hours = timeInSeconds / 3600
    val minutes = (timeInSeconds % 3600) / 60
    val seconds = timeInSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
