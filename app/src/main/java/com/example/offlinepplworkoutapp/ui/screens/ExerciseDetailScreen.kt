package com.example.offlinepplworkoutapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.components.SetDataEntryDialog
import com.example.offlinepplworkoutapp.ui.theme.AmberAccent
import com.example.offlinepplworkoutapp.ui.theme.BackgroundLight
import com.example.offlinepplworkoutapp.ui.theme.CardBackground
import com.example.offlinepplworkoutapp.ui.theme.PrimaryCoral
import com.example.offlinepplworkoutapp.ui.theme.ProgressEnd
import com.example.offlinepplworkoutapp.ui.theme.ProgressStart
import com.example.offlinepplworkoutapp.ui.theme.SuccessGreen
import com.example.offlinepplworkoutapp.ui.theme.TealSecondary
import com.example.offlinepplworkoutapp.ui.theme.TextOnPrimary
import com.example.offlinepplworkoutapp.ui.theme.TextPrimary
import com.example.offlinepplworkoutapp.ui.theme.TextSecondary
import com.example.offlinepplworkoutapp.ui.viewmodel.ExerciseDetailViewModel
import com.example.offlinepplworkoutapp.ui.viewmodel.ExerciseDetailViewModelFactory
import com.example.offlinepplworkoutapp.util.HapticFeedbackHelper
import com.example.offlinepplworkoutapp.util.NotificationHelper
import com.example.offlinepplworkoutapp.util.rememberHapticFeedback
import kotlinx.coroutines.delay

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

    // 🚀 NEW: Rest timer state
    val restTimer by viewModel.restTimer.collectAsState()
    val isRestActive by viewModel.isRestActive.collectAsState()
    val restMinuteMilestoneReached by viewModel.restMinuteMilestoneReached.collectAsState()

    // Get context for notification and haptic feedback
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    // 📳 NEW: Add haptic feedback helper
    val hapticFeedback = rememberHapticFeedback()

    // 🔔 NEW: Notification for 1-minute rest milestone
    LaunchedEffect(restMinuteMilestoneReached) @androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE) {
        if (restMinuteMilestoneReached && isRestActive) {
            println("🔔 NOTIFICATION: Rest milestone reached, showing notification")
            notificationHelper.showRestTimerNotification(workoutEntry.exerciseName)

            // 📳 NEW: Add haptic feedback for milestone reached
            hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.SUCCESS)
        }
    }

    // 🔔 NEW: Reset notification when rest timer stops
    LaunchedEffect(isRestActive) {
        if (!isRestActive) {
            println("🔔 NOTIFICATION: Rest timer stopped, cancelling notifications")
            notificationHelper.cancelRestTimerNotification()
        }
    }

    // 🚀 NEW: Phase 2.1.2 - Set data entry dialog state
    val showSetDataDialog by viewModel.showSetDataDialog.collectAsState()
    val pendingSetData by viewModel.pendingSetData.collectAsState()

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
                            text = "Sets: $completedSets/${setTimers.size} • Total: ${formatTime(totalExerciseTime / 1000)}".also {
                                println("🕐 UI DEBUG: Displaying total time - Raw: ${totalExerciseTime}ms, Converted: ${totalExerciseTime / 1000}s, Formatted: ${formatTime(totalExerciseTime / 1000)}")
                            },
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
            // 🚀 NEW: Rest timer display at top of screen
            item {
                AnimatedVisibility(
                    visible = isRestActive,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    RestTimerCard(
                        restTime = restTimer / 1000, // Convert milliseconds to seconds
                        isActive = isRestActive
                    )
                }
            }

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

            // Set cards with improved design and delete functionality
            itemsIndexed(setTimers) { index, setTimer ->
                val activeSetIndex by viewModel.activeSetIndex.collectAsState()

                // Get performance data from view model
                val setData by viewModel.getSetData(index).collectAsState(initial = null)
                val repsPerformed = setData?.repsPerformed ?: 0
                val weightUsed = setData?.weightUsed ?: 0f

                ModernSetTimerCard(
                    setNumber = index + 1,
                    totalSets = setTimers.size,
                    targetReps = workoutEntry.reps,
                    setTimer = setTimer.elapsedTime / 1000, // 🔧 FIXED: Convert milliseconds to seconds
                    isCurrentSet = currentRunningSet == index,
                    isCompleted = setTimer.isCompleted,
                    isActive = index == activeSetIndex,
                    isLocked = index > activeSetIndex && !setTimer.isCompleted,
                    repsPerformed = repsPerformed,
                    weightUsed = weightUsed,
                    onStartTimer = { viewModel.startSetTimer(index) },
                    onStopTimer = { viewModel.stopSetTimer(index) },
                    onCompleteSet = { /* This callback is not needed anymore since we use onStopTimer */ },
                    onEditSet = { viewModel.editSetData(index) },
                    // 🆕 NEW: Add delete functionality for incomplete sets
                    onDeleteSet = if (!setTimer.isCompleted && setTimers.size > 1) {
                        { viewModel.removeSpecificSet(index) }
                    } else null
                )
            }

            // 🆕 NEW: Simple Add Set Button (replaces the bulky management card)
            item {
                val canAddSet = setTimers.size < 8 // Max 8 sets per exercise

                if (canAddSet) {
                    AddSetButton(
                        onAddSet = { viewModel.addSetWithReps() }
                    )
                }
            }
        }
    }

    // 🚀 NEW: Phase 2.1.2 - Set Data Entry Dialog
    if (showSetDataDialog) {
        pendingSetData?.let { (setIndex, _) ->
            // Get existing set data for editing mode
            val setData by viewModel.getSetData(setIndex).collectAsState(initial = null)
            val currentSetData = setData // Create a local variable for smart casting
            val isEditMode = currentSetData != null && currentSetData.isCompleted

            SetDataEntryDialog(
                setNumber = setIndex + 1,
                exerciseName = workoutEntry.exerciseName,
                onDataEntered = { performanceData ->
                    viewModel.submitSetPerformanceData(
                        repsPerformed = performanceData.repsPerformed,
                        weightUsed = performanceData.weightUsed
                    )
                },
                onCancel = if (isEditMode) {
                    // 🆕 NEW: Cancel callback for edit mode
                    { viewModel.dismissSetDataDialog() }
                } else null, // No cancel for new entries
                isRestTimerRunning = isRestActive && !isEditMode, // Don't show rest timer when editing
                restTimeFormatted = formatTime(restTimer / 1000),
                // 🆕 NEW: Edit mode parameters
                isEditMode = isEditMode,
                initialReps = currentSetData?.repsPerformed ?: 0,
                initialWeight = currentSetData?.weightUsed ?: 0f
            )
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
    repsPerformed: Int = 0,
    weightUsed: Float = 0f,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onCompleteSet: () -> Unit,
    onEditSet: () -> Unit, // 🆕 NEW: Edit action
    onDeleteSet: (() -> Unit)? = null // 🆕 NEW: Delete action
) {
    // 📳 NEW: Add haptic feedback helper
    val hapticFeedback = rememberHapticFeedback()

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 🆕 NEW: Delete button for incomplete sets
                    if (onDeleteSet != null) {
                        IconButton(
                            onClick = onDeleteSet,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete set",
                                tint = PrimaryCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Timer display with improved styling
                    TimerDisplay(
                        time = setTimer,
                        isActive = isCurrentSet,
                        isCompleted = isCompleted
                    )
                }
            }

            // Target reps info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Target: $targetReps reps",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                )

                // 🚀 NEW: Display performance data when set is completed
                if (isCompleted && (repsPerformed > 0 || weightUsed > 0f)) {
                    Text(
                        text = buildString {
                            if (repsPerformed > 0) append("$repsPerformed reps")
                            if (repsPerformed > 0 && weightUsed > 0f) append(" × ")
                            if (weightUsed > 0f) append("${weightUsed}lbs")
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryCoral,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Action buttons with modern design
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    isCompleted -> {
                        // Completed state - show success message and performance data
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
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

                                // 🚀 NEW: Detailed performance data display
                                if (repsPerformed > 0 || weightUsed > 0f) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        if (repsPerformed > 0) {
                                            Text(
                                                text = "Performed: $repsPerformed reps",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary
                                                )
                                            )
                                        }
                                        if (repsPerformed > 0 && weightUsed > 0f) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        if (weightUsed > 0f) {
                                            Text(
                                                text = "Weight: ${weightUsed}lbs",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // 🆕 NEW: Edit button for completed sets
                            IconButton(
                                onClick = @androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE) {
                                    // 📳 NEW: Add haptic feedback for edit action
                                    hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.BUTTON_PRESS)
                                    onEditSet()
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit set data",
                                    tint = PrimaryCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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
                        // 🚀 NEW: Phase 2.1.2 - Just stop timer, dialog will handle completion
                        Button(
                            onClick = {
                                // 📳 NEW: Add haptic feedback when completing a set
                                hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.SUCCESS)

                                // 🔧 FIX: Call onStopTimer to stop the timer and trigger the dialog
                                onStopTimer()
                                println("🔧 DEBUG: Complete Set button clicked - stopping timer")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Stop and Complete",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete Set")
                        }
                    }
                    else -> {
                        // Ready to start
                        Button(
                            onClick = {
                                // 📳 NEW: Add haptic feedback when starting a set timer
                                hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.TIMER_START_STOP)
                                onStartTimer()
                            },
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

    // 🔧 PROPER STOPWATCH: Start from stored time and count up properly
    var displayTime by remember(time) { mutableStateOf(time) }

    // Track when timer starts to calculate elapsed time properly
    val startTimeRef = remember { mutableStateOf(0L) }

    LaunchedEffect(isActive) {
        if (isActive) {
            // Record the actual start time when timer becomes active
            startTimeRef.value = System.currentTimeMillis()

            while (isActive) {
                // Calculate elapsed seconds since timer started
                val elapsedMs = System.currentTimeMillis() - startTimeRef.value
                val elapsedSeconds = elapsedMs / 1000

                // Display = stored time + elapsed time
                displayTime = time + elapsedSeconds

                delay(1000) // Update every second
            }
        } else {
            // When not active, show the stored time from database
            displayTime = time
        }
    }

    Text(
        text = formatTime(displayTime),
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = if (isActive) 20.sp else 18.sp,
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

// 🚀 NEW: Rest Timer Card Component
@Composable
fun RestTimerCard(
    restTime: Long,
    isActive: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = AmberAccent.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rest icon - using a pause/timer-like icon
                Icon(
                    imageVector = Icons.Default.PlayArrow, // We'll use this as a rest indicator
                    contentDescription = "Rest",
                    tint = AmberAccent,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Rest Timer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            // Rest time display with prominent styling
            Text(
                text = formatTime(restTime),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AmberAccent,
                    fontSize = 24.sp
                )
            )
        }
    }
}

// 🆕 NEW: 2.2.2 - Dynamic Set Management Controls with simplified UI
@Composable
fun AddSetButton(
    onAddSet: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with description
            Text(
                text = "Add New Set",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = "Add a new set using the default target repetitions for this exercise.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            )

            // Add set button
            Button(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryCoral
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Set",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Set")
            }

            // Help text
            Text(
                text = "Maximum 8 sets per exercise.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
