package com.example.offlinepplworkoutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.entity.Exercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
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
            workoutEntryDao = database.workoutEntryDao()
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
    val viewModel: DailyWorkoutViewModel = viewModel(
        factory = DailyWorkoutViewModelFactory(repository)
    )

    var showDebugMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            // Only show debug FAB in debug builds
            if (IS_DEBUG_MODE) {
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
        DailyWorkoutScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )

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
                }
            )
        }
    }
}

@Composable
fun DailyWorkoutScreen(
    viewModel: DailyWorkoutViewModel,
    modifier: Modifier = Modifier
) {

    val todaysWorkout by viewModel.todaysWorkout.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val completionProgress by viewModel.completionProgress.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with day and workout type
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
            // Rest Day with Background Image
            RestDayScreen()
        } else {
            Text(
                text = "Exercises: ${todaysWorkout.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(todaysWorkout) { workoutEntry ->
                    WorkoutExerciseItem(
                        workoutEntry = workoutEntry,
                        onCompletionToggle = { viewModel.toggleExerciseCompletion(workoutEntry.id) }
                    )
                }
            }
        }
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
fun WorkoutExerciseItem(workoutEntry: WorkoutEntryWithExercise, onCompletionToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                    text = "Completed",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = workoutEntry.isCompleted,
                    onCheckedChange = { onCompletionToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
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
    onResetToToday: () -> Unit
) {
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