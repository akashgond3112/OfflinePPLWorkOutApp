package com.example.offlinepplworkoutapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.screens.exercisedetails.ExerciseDetailScreen
import com.example.offlinepplworkoutapp.ui.viewmodel.DailyWorkoutViewModel
import com.example.offlinepplworkoutapp.ui.viewmodel.DailyWorkoutViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val IS_DEBUG_MODE = true

@Composable
fun MainScreen(
    repository: WorkoutRepository
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: DailyWorkoutViewModel = viewModel(
        factory = DailyWorkoutViewModelFactory(repository)
    )

    var showDebugMenu by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<WorkoutEntryWithExercise?>(null) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showTemplateSelection by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val coroutineScope = rememberCoroutineScope()

    // 🔄 NEW: Back handler to override back button behavior
    BackHandler(
        enabled = selectedExercise != null || showTemplateSelection,
        onBack = {
            when {
                selectedExercise != null -> {
                    // Return to main workout screen when back is pressed in exercise detail
                    println("🔙 NAVIGATION: Back button pressed in exercise detail, returning to main screen")
                    viewModel.refreshTodaysWorkout() // Refresh data
                    selectedExercise = null
                }

                showTemplateSelection -> {
                    // Return from template selection
                    println("🔙 NAVIGATION: Back button pressed in template selection, returning to main screen")
                    showTemplateSelection = false
                }
            }
        }
    )

    // Add reset confirmation dialog
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = {
                Text(
                    text = "⚠️ Reset Workout Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "This will clear all your workout progress and history, but keep your exercise library intact.\n\nThis action cannot be undone. Are you sure you want to continue?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Use coroutineScope.launch to call the suspend function properly
                        coroutineScope.launch {
                            println("🚀 UI: Starting reset process...")

                            // Verify what's in database before reset
                            val beforeReset = PPLWorkoutDatabase.verifyDatabaseEmpty()
                            println("📊 BEFORE RESET: ${beforeReset.first} days, ${beforeReset.second} entries, ${beforeReset.third} sets")

                            // Use the more aggressive reset that forces database recreation
                            PPLWorkoutDatabase.forceResetDatabase(context)

                            // Wait a moment for database operations to complete
                            kotlinx.coroutines.delay(500)

                            // Verify database is actually empty after reset
                            val afterReset = PPLWorkoutDatabase.verifyDatabaseEmpty()
                            println("📊 AFTER RESET: ${afterReset.first} days, ${afterReset.second} entries, ${afterReset.third} sets")

                            // Force complete refresh of the ViewModel data
                            println("🔄 UI: Forcing ViewModel refresh...")
                            viewModel.forceCompleteRefresh()

                            // Close dialogs
                            showResetConfirmation = false
                            showDebugMenu = false

                            println("✅ UI: Reset process complete!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset All Progress")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            // Only show debug FAB in debug builds when not in exercise detail
            if (IS_DEBUG_MODE && selectedExercise == null) {
                FloatingActionButton(
                    onClick = { showDebugMenu = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Debug Day Selector"
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            showTemplateSelection -> {
                // Show Template Selection Screen
                com.example.offlinepplworkoutapp.ui.screens.TemplateSelectionScreen(
                    repository = repository,
                    selectedDate = viewModel.currentDate.collectAsState().value,
                    onTemplateSelected = { template ->
                        viewModel.selectTemplate(template)
                        viewModel.createWorkoutFromSelectedTemplate()
                        showTemplateSelection = false
                    },
                    onBackClick = { showTemplateSelection = false }
                )
            }

            selectedExercise != null -> {
                // Show Exercise Detail Screen
                ExerciseDetailScreen(
                    workoutEntry = selectedExercise!!,
                    repository = repository,
                    onBackClick = {
                        // Force a refresh of workout data when returning from detail screen
                        viewModel.refreshTodaysWorkout()
                        selectedExercise = null
                        viewModel.refreshTodaysWorkout()
                    },
                    onSaveChanges = { sets, reps, isCompleted ->
                        viewModel.updateExercise(selectedExercise!!.id, sets, reps, isCompleted)
                        // Force a refresh to ensure updated set count is displayed
                        viewModel.refreshTodaysWorkout()
                    }
                )
            }

            else -> {
                // Show Daily Workout Screen
                DailyWorkoutScreen(
                    viewModel = viewModel,
                    repository = repository,
                    onExerciseClick = { exercise -> selectedExercise = exercise },
                    onTemplateSelectionClick = { showTemplateSelection = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Debug day selector dialog
        println("🔧 UI: Debug menu visibility is $showDebugMenu" + "IS_DEBUG_MODE is $IS_DEBUG_MODE")
        if (showDebugMenu && IS_DEBUG_MODE) {
            DebugDaySelector(
                onDaySelected = { dayOfWeek ->
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, dayOfWeek)
                    }
                    val debugDate = dateFormat.format(calendar.time)
                    viewModel.setDebugDate(debugDate)
                    showDebugMenu = false
                },
                onDismiss = { showDebugMenu = false }
            )
        }
    }
}
