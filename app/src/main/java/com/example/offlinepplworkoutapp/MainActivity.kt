package com.example.offlinepplworkoutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DailyWorkoutScreen(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyWorkoutScreen(
    repository: WorkoutRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: DailyWorkoutViewModel = viewModel(
        factory = DailyWorkoutViewModelFactory(repository)
    )

    val todaysWorkout by viewModel.todaysWorkout.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Rest Day - No workout scheduled",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
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
                    WorkoutExerciseItem(workoutEntry = workoutEntry)
                }
            }
        }
    }
}

@Composable
fun WorkoutExerciseItem(workoutEntry: WorkoutEntryWithExercise) {
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
            exerciseName = "Barbell Bench Press",
            isCompound = true
        )
        WorkoutExerciseItem(workoutEntry = sampleWorkoutEntry)
    }
}