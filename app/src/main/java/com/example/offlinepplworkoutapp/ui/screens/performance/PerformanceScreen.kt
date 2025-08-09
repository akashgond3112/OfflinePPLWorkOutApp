package com.example.offlinepplworkoutapp.ui.screens.performance

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.performance.ExercisePerformance
import com.example.offlinepplworkoutapp.ui.components.performance.ChartType
import com.example.offlinepplworkoutapp.ui.components.performance.PerformanceChart
import com.example.offlinepplworkoutapp.ui.components.performance.PerformanceTabRow
import kotlin.math.absoluteValue

/**
 * Main composable for the Performance tab screen
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PerformanceScreen(database: PPLWorkoutDatabase) {
    // Create ViewModel through factory
    val viewModel: PerformanceViewModel = viewModel(
        factory = PerformanceViewModel.Factory(database)
    )

    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Main screen content
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header with title, subtitle, and period selector
            PerformanceHeader(
                selectedDays = uiState.selectedTimePeriod,
                onDaysSelected = { days -> viewModel.loadPerformanceData(days) },
                selectedMuscle = uiState.selectedMuscle,
                onBackPressed = { viewModel.clearSelectedMuscle() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show loading indicator or content
            if (uiState.isLoading) {
                LoadingShimmerEffect()
            } else {
                // If no muscle is selected, show muscle groups
                if (uiState.selectedMuscle == null) {
                    MuscleGroupGrid(
                        exercisePerformances = uiState.exercisePerformances,
                        onMuscleSelected = { muscle -> viewModel.selectMuscle(muscle) }
                    )
                } else {
                    // Show exercises for the selected muscle
                    val filteredExercises = uiState.exercisePerformances.filter {
                        it.exercise.primaryMuscle == uiState.selectedMuscle
                    }

                    ExercisesList(
                        exercisePerformances = filteredExercises,
                        expandedExercises = uiState.expandedExercises,
                        onToggleExpanded = { exerciseId -> viewModel.toggleExerciseExpanded(exerciseId) }
                    )
                }
            }
        }
    }
}

/**
 * Header with title, subtitle, period selector, and optional back button
 */
@Composable
fun PerformanceHeader(
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit,
    selectedMuscle: String?,
    onBackPressed: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title row with optional back button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show back button if a muscle is selected
                if (selectedMuscle != null) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to muscle groups",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = selectedMuscle ?: "Performance",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Period selector with pill shape
            PeriodSelector(
                selectedDays = selectedDays,
                onDaysSelected = onDaysSelected
            )
        }

        // Only show subtitle on the main performance screen
        if (selectedMuscle == null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your progress overview",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Pill-shaped period selector with dropdown
 */
@Composable
fun PeriodSelector(
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val timeOptions = listOf(7, 14, 30)

    Box {
        // Pill-shaped button
        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { expanded = true },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDays}d",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Select time period",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            timeOptions.forEach { days ->
                DropdownMenuItem(
                    text = { Text("${days}d") },
                    onClick = {
                        onDaysSelected(days)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Loading shimmer effect for a more polished UX
 */
@Composable
fun LoadingShimmerEffect() {
    // Create infinite transition for shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Shimmer grid placeholder
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                                    .copy(alpha = alpha)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loading your performance data...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Grid of muscle group cards in a 2-column layout
 */
@Composable
fun MuscleGroupGrid(
    exercisePerformances: List<ExercisePerformance>,
    onMuscleSelected: (String) -> Unit
) {
    // Group exercises by primary muscle
    val muscleGroups = exercisePerformances
        .map { it.exercise.primaryMuscle }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    if (muscleGroups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                Text(
                    text = "No exercise data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Complete workouts to see your performance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),  // Reduced spacing
            verticalArrangement = Arrangement.spacedBy(8.dp)     // Reduced spacing
        ) {
            items(muscleGroups) { muscle ->
                val exercisesForMuscle = exercisePerformances.filter {
                    it.exercise.primaryMuscle == muscle
                }

                // Use interactionSource to track pressed state
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // Muscle group card with press animation - reduced size
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)  // Changed from 1f to 1.5f to make card shorter
                        .graphicsLayer {
                            // Scale down slightly when pressed
                            val scale = if (isPressed) 0.96f else 1f
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null // Remove ripple effect as we have our own animation
                        ) { onMuscleSelected(muscle) },
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 2.dp  // Reduced from 4dp to 2dp
                    ),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    radius = 500f
                                )
                            )
                            .padding(12.dp)  // Reduced padding from 16dp to 12dp
                    ) {
                        // Muscle icon at the top - reduced size
                        getMuscleIcon(muscle)?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)  // Reduced from 40dp to 28dp
                                    .align(Alignment.TopStart),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Text info at the bottom
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = muscle,
                                style = MaterialTheme.typography.bodyLarge,  // Changed from titleMedium to bodyLarge
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${exercisesForMuscle.size} ${if (exercisesForMuscle.size == 1) "exercise" else "exercises"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Arrow indicator - reduced size
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "View exercises",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)  // Reduced from 20dp to 16dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Function to get the appropriate icon for each muscle group
 */
@Composable
fun getMuscleIcon(muscleName: String): ImageVector? {
    return when (muscleName.lowercase()) {
        "chest" -> Icons.Default.FitnessCenter
        "back" -> Icons.Default.FitnessCenter
        "shoulders" -> Icons.Default.FitnessCenter
        "biceps" -> Icons.Default.FitnessCenter
        "triceps" -> Icons.Default.FitnessCenter
        "legs" -> Icons.Default.FitnessCenter
        "glutes" -> Icons.Default.FitnessCenter
        "hamstrings" -> Icons.Default.FitnessCenter
        "quadriceps" -> Icons.Default.FitnessCenter
        "calves" -> Icons.Default.FitnessCenter
        "abs" -> Icons.Default.FitnessCenter
        "forearms" -> Icons.Default.FitnessCenter
        else -> Icons.Default.FitnessCenter
    }
}

/**
 * List of exercises for a selected muscle group
 */
@Composable
fun ExercisesList(
    exercisePerformances: List<ExercisePerformance>,
    expandedExercises: Set<Int>,
    onToggleExpanded: (Int) -> Unit
) {
    if (exercisePerformances.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No exercises found for this muscle group",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(exercisePerformances) { performance ->
                ExercisePerformanceCard(
                    performance = performance,
                    isExpanded = expandedExercises.contains(performance.exercise.id),
                    onToggleExpanded = { onToggleExpanded(performance.exercise.id) }
                )
            }
        }
    }
}

/**
 * Card displaying exercise performance data
 */
@Composable
fun ExercisePerformanceCard(
    performance: ExercisePerformance,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val expandTransition = remember { Animatable(0f) }
    val exerciseId = performance.exercise.id
    val exerciseName = performance.exercise.name

    // Log when the card is rendered
    Log.d("PerformanceScreen", "Rendering ExercisePerformanceCard: ID=${exerciseId}, Name=${exerciseName}, Expanded=${isExpanded}")

    // Log performance data
    Log.d("PerformanceScreen", "Performance data for ${exerciseName}: " +
            "MaxWeight=${performance.maxWeight}kg, " +
            "MaxReps=${performance.maxReps}, " +
            "VolumeProg=${performance.volumeProgress}%, " +
            "WeightProg=${performance.weightProgress}%, " +
            "Sessions=${performance.sessionsCount}, " +
            "DataPoints=${performance.progressData.size}"
    )

    // Animate the expansion/collapse
    LaunchedEffect(isExpanded) {
        Log.d("PerformanceScreen", "Animating card expansion: ${exerciseName} to ${if (isExpanded) "expanded" else "collapsed"}")
        expandTransition.animateTo(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(300)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Exercise header (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = performance.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(expandTransition.value * 180f)
                )
            }

            // Performance details (only visible when expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Performance visualization with tabs
                    var selectedTabIndex by remember { mutableStateOf(0) }

                    PerformanceTabRow(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show the appropriate chart based on the selected tab
                    PerformanceChart(
                        progressPoints = performance.progressData,
                        chartType = when (selectedTabIndex) {
                            0 -> ChartType.WEIGHT
                            1 -> ChartType.REPS
                            else -> ChartType.VOLUME
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress summary
                    ProgressSummaryCard(performance = performance)
                }
            }
        }
    }
}

/**
 * Card displaying a summary of progress metrics
 */
@Composable
fun ProgressSummaryCard(performance: ExercisePerformance) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progress Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem(
                    label = "Max Weight",
                    value = "${performance.maxWeight} kg",
                    icon = Icons.Default.FitnessCenter
                )
                MetricItem(
                    label = "Max Reps",
                    value = "${performance.maxReps}",
                    icon = Icons.Default.Numbers
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val volumeProgress = performance.volumeProgress
                val weightProgress = performance.weightProgress

                // Calculate rep progress - similar to how weight progress is calculated
                // Can be 0 if there's only one data point
                val repProgress = if (performance.progressData.size >= 2) {
                    val firstReps = performance.progressData.first().reps
                    val lastReps = performance.progressData.last().reps
                    if (firstReps > 0) {
                        ((lastReps - firstReps).toFloat() / firstReps * 100f)
                    } else 0f
                } else 0f

                ProgressMetricItem(
                    label = "Rep Progress",
                    value = "${String.format(java.util.Locale.US, "%.1f", repProgress)}%",
                    progress = repProgress / 100f,
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )

                ProgressMetricItem(
                    label = "Weight Progress",
                    value = "${String.format(java.util.Locale.US, "%.1f", weightProgress)}%",
                    progress = weightProgress / 100f,
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )

                ProgressMetricItem(
                    label = "Volume Progress",
                    value = "${String.format(java.util.Locale.US, "%.1f", volumeProgress)}%",
                    progress = volumeProgress / 100f,
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
            }

            if (performance.sessionsCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Based on ${performance.sessionsCount} training sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * Single metric item display with icon
 */
@Composable
fun MetricItem(label: String, value: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Progress metric item with progress indicator
 */
@Composable
fun ProgressMetricItem(label: String, value: String, progress: Float, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        val progressColor = when {
            progress > 0 -> MaterialTheme.colorScheme.primary
            progress < 0 -> Color.Red
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = progressColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = if (progress == 0f && value == "0.0%") "--" else value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = progressColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Small progress indicator - only show if there's actual progress
        if (progress != 0f || value != "0.0%") {
            LinearProgressIndicator(
                progress = { progress.absoluteValue.coerceIn(0f, 1f) },
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .width(60.dp)
                    .height(3.dp)
            )
        }
    }
}
