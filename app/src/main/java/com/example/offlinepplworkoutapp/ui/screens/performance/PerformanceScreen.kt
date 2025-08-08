package com.example.offlinepplworkoutapp.ui.screens.performance

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar with time period dropdown
            PerformanceTopBar(
                selectedDays = uiState.selectedTimePeriod,
                onDaysSelected = { days -> viewModel.loadPerformanceData(days) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show loading indicator or content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Exercise list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.exercisePerformances) { performance ->
                        ExercisePerformanceCard(
                            performance = performance,
                            isExpanded = uiState.expandedExercises.contains(performance.exercise.id),
                            onToggleExpanded = { viewModel.toggleExerciseExpanded(performance.exercise.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top bar with time period selector
 */
@Composable
fun PerformanceTopBar(
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val timeOptions = listOf(7, 14, 30)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Performance",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Box {
            // Selected time period button
            IconButton(onClick = { expanded = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${selectedDays}d")
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select time period"
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

    // Animate the expansion/collapse
    LaunchedEffect(isExpanded) {
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

                ProgressMetricItem(
                    label = "Volume Progress",
                    value = "${String.format(java.util.Locale.US, "%.1f", volumeProgress)}%",
                    progress = volumeProgress / 100f,
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
                ProgressMetricItem(
                    label = "Weight Progress",
                    value = "${String.format(java.util.Locale.US, "%.1f", weightProgress)}%",
                    progress = weightProgress / 100f,
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
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = progressColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Small progress indicator
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
