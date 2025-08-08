package com.example.offlinepplworkoutapp.ui.screens.exercisedetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.components.SetDataEntryDialog
import com.example.offlinepplworkoutapp.ui.components.exercise.AddSetButton
import com.example.offlinepplworkoutapp.ui.components.exercise.ExerciseCompletionCard
import com.example.offlinepplworkoutapp.ui.components.exercise.ExerciseDetailHeader
import com.example.offlinepplworkoutapp.ui.components.exercise.RestTimerCard
import com.example.offlinepplworkoutapp.ui.components.exercise.SetTimerCard
import com.example.offlinepplworkoutapp.ui.viewmodel.ExerciseDetailViewModel
import com.example.offlinepplworkoutapp.ui.viewmodel.ExerciseDetailViewModelFactory
import com.example.offlinepplworkoutapp.utils.ExerciseDetailUtils.formatTime
import com.example.offlinepplworkoutapp.utils.HapticFeedbackHelper
import com.example.offlinepplworkoutapp.utils.NotificationHelper
import com.example.offlinepplworkoutapp.utils.rememberHapticFeedback
import kotlinx.coroutines.launch

@Composable
fun ExerciseDetailScreen(
    workoutEntry: WorkoutEntryWithExercise,
    repository: WorkoutRepository,
    onBackClick: () -> Unit,
    onSaveChanges: (sets: Int, reps: Int, isCompleted: Boolean) -> Unit
) {
    // ViewModel setup with unique key for each exercise
    val viewModel: ExerciseDetailViewModel = viewModel(
        key = "exercise_${workoutEntry.id}",
        factory = ExerciseDetailViewModelFactory(workoutEntry, repository)
    )

    // Collect state from ViewModel
    val setTimers by viewModel.setTimers.collectAsState()
    val currentRunningSet by viewModel.currentRunningSet.collectAsState()
    val totalExerciseTime by viewModel.totalExerciseTime.collectAsState()
    val completedSets by viewModel.completedSets.collectAsState()
    val isExerciseCompleted by viewModel.isExerciseCompleted.collectAsState()
    val restTimer by viewModel.restTimer.collectAsState()
    val isRestActive by viewModel.isRestActive.collectAsState()
    val restMinuteMilestoneReached by viewModel.restMinuteMilestoneReached.collectAsState()
    val showSetDataDialog by viewModel.showSetDataDialog.collectAsState()
    val pendingSetData by viewModel.pendingSetData.collectAsState()

    // Track deleted set for animation
    var deletedSetIndex by remember { mutableStateOf<Int?>(null) }

    // Snackbar for user feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Setup notifications and haptic feedback
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    val hapticFeedback = rememberHapticFeedback()
    val originalCompletionStatus = remember { workoutEntry.isCompleted }

    // Handle rest timer milestone notifications
    LaunchedEffect(restMinuteMilestoneReached) @androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE) {
        if (restMinuteMilestoneReached && isRestActive) {
            notificationHelper.showRestTimerNotification(workoutEntry.exerciseName)
            hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.SUCCESS)
        }
    }

    // Cancel notifications when rest timer stops
    LaunchedEffect(isRestActive) {
        if (!isRestActive) {
            notificationHelper.cancelRestTimerNotification()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ExerciseDetailHeader(
                exerciseName = workoutEntry.exerciseName,
                completedSets = completedSets,
                totalSets = setTimers.size,
                totalTime = totalExerciseTime,
                onBackClick = {
                    // Check if sets count changed or completion status changed
                    val newTotalSets = setTimers.size
                    val shouldMarkCompleted =
                        isExerciseCompleted && completedSets == newTotalSets

                    // Always save changes if either sets changed or completion status changed
                    if (newTotalSets != workoutEntry.sets || shouldMarkCompleted != originalCompletionStatus) {
                        onSaveChanges(newTotalSets, workoutEntry.reps, shouldMarkCompleted)
                    }
                    onBackClick()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = rememberLazyListState()
        ) {
            // Rest timer display
            item {
                AnimatedVisibility(
                    visible = isRestActive,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    RestTimerCard(
                        restTime = restTimer / 1000
                    )
                }
            }

            // Exercise completion status
            item {
                AnimatedVisibility(
                    visible = isExerciseCompleted,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ExerciseCompletionCard()
                }
            }

            // Set timer cards
            itemsIndexed(setTimers) { index, setTimer ->
                val activeSetIndex by viewModel.activeSetIndex.collectAsState()
                val setData by viewModel.getSetData(index).collectAsState(initial = null)

                AnimatedVisibility(
                    visible = deletedSetIndex != index,
                    enter = fadeIn(),
                    exit = slideOutHorizontally(
                        animationSpec = tween(durationMillis = 400)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 300)
                    )
                ) {
                    SetTimerCard(
                        setNumber = index + 1,
                        totalSets = setTimers.size,
                        targetReps = workoutEntry.reps,
                        setTimer = setTimer.elapsedTime / 1000,
                        isCurrentSet = currentRunningSet == index,
                        isCompleted = setTimer.isCompleted,
                        isActive = index == activeSetIndex,
                        isLocked = index > activeSetIndex && !setTimer.isCompleted,
                        repsPerformed = setData?.repsPerformed ?: 0,
                        weightUsed = setData?.weightUsed ?: 0f,
                        onStartTimer = { viewModel.startSetTimer(index) },
                        onStopTimer = { viewModel.stopSetTimer(index) },
                        onEditSet = { viewModel.editSetData(index) },
                        onDeleteSet = if (!setTimer.isCompleted && setTimers.size > 1) {
                            {
                                // Store index of set being deleted for animation
                                deletedSetIndex = index

                                // Provide haptic feedback when deleting
                                hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.HEAVY_CLICK)

                                // Show deletion animation for a brief moment before actual deletion
                                coroutineScope.launch {
                                    // Show snackbar confirmation
                                    snackbarHostState.showSnackbar(
                                        message = "Set ${index + 1} deleted",
                                        duration = SnackbarDuration.Short
                                    )

                                    // Delay actual deletion to allow animation to play
                                    kotlinx.coroutines.delay(300)
                                    viewModel.removeSpecificSet(index)

                                    // Reset deleted set index after a delay
                                    kotlinx.coroutines.delay(100)
                                    deletedSetIndex = null
                                }
                            }
                        } else null,
                        onResetSet = if (setTimer.isCompleted) {
                            { viewModel.resetSet(index) }
                        } else null
                    )
                }
            }

            // Add set button
            item {
                val canAddSet = setTimers.size < 8
                if (canAddSet) {
                    AddSetButton(
                        onAddSet = { viewModel.addSetWithReps() },
                        canAddSet = canAddSet
                    )
                }
            }
        }
    }

    // Set data entry dialog
    if (showSetDataDialog) {
        pendingSetData?.let { (setIndex, _) ->
            val setData by viewModel.getSetData(setIndex).collectAsState(initial = null)
            val currentSetData = setData
            val isEditMode = currentSetData != null && currentSetData.isCompleted

            SetDataEntryDialog(
                setNumber = setIndex + 1,
                exerciseName = workoutEntry.exerciseName,
                onDataEntered = { performanceData ->
                    // Calculate actual weight based on dumbbell selection
                    val actualWeight = if (performanceData.isDoubleDumbbell) {
                        // Double the weight for double dumbbells
                        performanceData.weightUsed * 2
                    } else {
                        // Use as-is for single dumbbell/plate
                        performanceData.weightUsed
                    }

                    // Log the weight calculation
                    println("🏋️ WEIGHT: Original input: ${performanceData.weightUsed}lbs, " +
                            "Double dumbbell: ${performanceData.isDoubleDumbbell}, " +
                            "Actual weight: ${actualWeight}lbs")

                    viewModel.submitSetPerformanceData(
                        repsPerformed = performanceData.repsPerformed,
                        weightUsed = actualWeight
                    )
                },
                onCancel = if (isEditMode) {
                    { viewModel.dismissSetDataDialog() }
                } else null,
                isRestTimerRunning = isRestActive && !isEditMode,
                restTimeFormatted = formatTime(restTimer / 1000),
                isEditMode = isEditMode,
                initialReps = currentSetData?.repsPerformed ?: 0,
                initialWeight = currentSetData?.weightUsed ?: 0f
            )
        }
    }
}