package com.example.offlinepplworkoutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.screens.ExerciseDetailScreen
import com.example.offlinepplworkoutapp.ui.theme.OfflinePPLWorkOutAppTheme
import com.example.offlinepplworkoutapp.ui.viewmodel.DailyWorkoutViewModel
import com.example.offlinepplworkoutapp.ui.viewmodel.DailyWorkoutViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

private const val IS_DEBUG_MODE = true

class MainActivity : ComponentActivity() {

    private lateinit var database: PPLWorkoutDatabase
    private lateinit var repository: WorkoutRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repository
        database = PPLWorkoutDatabase.getDatabase(this)
        repository = WorkoutRepository(
            workoutDayDao = database.workoutDayDao(),
            workoutEntryDao = database.workoutEntryDao(),
            setEntryDao = database.setEntryDao()
        )

        enableEdgeToEdge()
        setContent {
            OfflinePPLWorkOutAppTheme {
                MainScreen(repository = repository)
            }
        }
    }
}

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
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val coroutineScope = rememberCoroutineScope()

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
        if (selectedExercise != null) {
            // Show Exercise Detail Screen
            ExerciseDetailScreen(
                workoutEntry = selectedExercise!!,
                repository = repository,
                onBackClick = { selectedExercise = null },
                onSaveChanges = { sets, reps, isCompleted ->
                    viewModel.updateExercise(selectedExercise!!.id, sets, reps, isCompleted)
                }
            )
        } else {
            // Show Daily Workout Screen
            DailyWorkoutScreen(
                viewModel = viewModel,
                repository = repository,
                onExerciseClick = { exercise -> selectedExercise = exercise },
                modifier = Modifier.padding(innerPadding)
            )
        }

        // Debug day selector dialog
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
                onDismiss = { showDebugMenu = false },
                onResetToToday = {
                    viewModel.setDebugDate(null)
                    showDebugMenu = false
                },
                onResetDatabase = {
                    // Show confirmation dialog before resetting the database
                    showResetConfirmation = true
                }
            )
        }
    }
}

@Composable
fun DailyWorkoutScreen(
    viewModel: DailyWorkoutViewModel,
    repository: WorkoutRepository,
    modifier: Modifier = Modifier,
    onExerciseClick: (WorkoutEntryWithExercise) -> Unit = {}
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
                    }
                )
            }
        } else {
            Text(
                text = "Exercises: ${todaysWorkout.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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

@Composable
fun WorkoutProgressIndicator(
    progress: Float,
    completionPercentage: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$completionPercentage%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (completionPercentage == 100)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun WorkoutExerciseItem(workoutEntry: WorkoutEntryWithExercise, onCompletionToggle: () -> Unit, onClick: (WorkoutEntryWithExercise) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(workoutEntry) }, // Pass the workout entry when clicked
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

            // Completion toggle switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (workoutEntry.isCompleted) "Completed" else "Start",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (workoutEntry.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        Color(0xFF4CAF50), // Green color for "Start"
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = workoutEntry.isCompleted,
                    onCheckedChange = { onCompletionToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color(0xFF4CAF50), // Green thumb when not completed
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.3f) // Light green track
                    )
                )
            }
        }
    }
}

@Composable
fun WorkoutExerciseItemWithTimer(workoutEntry: WorkoutEntryWithExercise, onCompletionToggle: () -> Unit, onClick: (WorkoutEntryWithExercise) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(workoutEntry) }, // Pass the workout entry when clicked
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

            // Completion toggle switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (workoutEntry.isCompleted) "Completed" else "Start",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (workoutEntry.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        Color(0xFF4CAF50), // Green color for "Start"
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = workoutEntry.isCompleted,
                    onCheckedChange = { onCompletionToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color(0xFF4CAF50), // Green thumb when not completed
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.3f) // Light green track
                    )
                )
            }

            // Timer display for total time spent (if exercise has time recorded)
            if (workoutEntry.totalSecondsSpent > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Time Spent",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Time spent: ${workoutEntry.totalSecondsSpent / 60}:${String.format(Locale.getDefault(), "%02d", workoutEntry.totalSecondsSpent % 60)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutExerciseItemWithSetProgress(workoutEntry: WorkoutEntryWithExercise, onClick: (WorkoutEntryWithExercise) -> Unit = {}, repository: WorkoutRepository) {
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
            val progress = if (workoutEntry.sets > 0) completedSetsCount.toFloat() / workoutEntry.sets.toFloat() else 0f

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
                            text = "${workoutEntry.totalSecondsSpent / 60}:${String.format("%02d", workoutEntry.totalSecondsSpent % 60)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestDayScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image - your awesome Hanuman gym mural
        Image(
            painter = painterResource(id = R.drawable.rest_day),
            contentDescription = "Rest Day Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay content with semi-transparent background for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🧘‍♂️",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Rest Day",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Recovery is just as important as training",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Take time to rest, stretch, and prepare for tomorrow's workout!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DebugDaySelector(
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onResetToToday: () -> Unit,
    onResetDatabase: () -> Unit = {}  // Added parameter for database reset
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🔧 Debug Day Selector",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = "Select a day to test different workouts:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                val days = listOf(
                    Calendar.MONDAY to "Monday - Push Day 1 💪",
                    Calendar.TUESDAY to "Tuesday - Pull Day 1 🏋️",
                    Calendar.WEDNESDAY to "Wednesday - Legs Day 1 🦵",
                    Calendar.THURSDAY to "Thursday - Push Day 2 💪",
                    Calendar.FRIDAY to "Friday - Pull Day 2 🏋️",
                    Calendar.SATURDAY to "Saturday - Legs Day 2 🦵",
                    Calendar.SUNDAY to "Sunday - Rest Day 🧘‍♂️"
                )

                items(days) { (dayOfWeek, dayLabel) ->
                    OutlinedButton(
                        onClick = { onDaySelected(dayOfWeek) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (dayOfWeek == Calendar.SUNDAY)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Debug Actions:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                println("🔍 DEBUG: Checking current database state...")
                                val currentState = PPLWorkoutDatabase.verifyDatabaseEmpty()
                                println("📊 CURRENT STATE: ${currentState.first} days, ${currentState.second} entries, ${currentState.third} sets")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("🔍 Check DB State")
                    }
                }

                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                println("🧨 DEBUG: Force reset database...")
                                PPLWorkoutDatabase.forceResetDatabase(context)
                                kotlinx.coroutines.delay(200)
                                val afterState = PPLWorkoutDatabase.verifyDatabaseEmpty()
                                println("📊 AFTER FORCE RESET: ${afterState.first} days, ${afterState.second} entries, ${afterState.third} sets")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("🧨 Force Reset DB")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onResetToToday) {
                Text("Reset to Today")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StartWorkoutScreen(
    workoutType: String,
    onStartWorkout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💪",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Ready to Start?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = workoutType,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "Tap the button below to create today's workout and start training!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Workout",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Start Today's Workout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutPreview() {
    OfflinePPLWorkOutAppTheme {
        // Preview with sample workout entry data
        val sampleWorkoutEntry = WorkoutEntryWithExercise(
            id = 1,
            dayId = 1,
            exerciseId = 1,
            sets = 4,
            reps = 8,
            isCompleted = false,
            exerciseName = "Barbell Bench Press",
            isCompound = true
        )
        WorkoutExerciseItem(workoutEntry = sampleWorkoutEntry, onCompletionToggle = {})
    }
}
