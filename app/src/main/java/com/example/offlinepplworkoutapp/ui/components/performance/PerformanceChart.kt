package com.example.offlinepplworkoutapp.ui.components.performance

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinepplworkoutapp.data.performance.ProgressPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A performance chart component that visualizes workout progression over time
 */
@Composable
fun PerformanceChart(
    progressPoints: List<ProgressPoint>,
    modifier: Modifier = Modifier,
    chartType: ChartType = ChartType.WEIGHT
) {
    // If there's no data, show a message
    if (progressPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available for selected time period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Track animation progress
    var animationPlayed by remember { mutableStateOf(false) }
    val animationProgress = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(1000),
        label = "chart animation"
    )

    // Start animation when component is first displayed
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    // Format the dates for display
    val dateFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    // Choose the data to display based on chart type
    val dataPoints = when (chartType) {
        ChartType.WEIGHT -> progressPoints.map { it.weight }
        ChartType.REPS -> progressPoints.map { it.reps.toFloat() }
        ChartType.VOLUME -> progressPoints.map { it.volume }
    }

    // Find min and max values for scaling
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val minValue = dataPoints.minOrNull() ?: 0f
    val range = (maxValue - minValue).coerceAtLeast(1f)

    // Get colors outside of Canvas since MaterialTheme.colorScheme can only be used in @Composable context
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    MaterialTheme.colorScheme.secondary

    // Main chart layout
    Column(modifier = modifier.fillMaxWidth()) {
        // Chart title based on type
        Text(
            text = when (chartType) {
                ChartType.WEIGHT -> "Weight Progression"
                ChartType.REPS -> "Repetition Progression"
                ChartType.VOLUME -> "Volume Progression"
            },
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // The actual chart visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(8.dp)
        ) {
            // Show current value when only one data point is available
            if (progressPoints.size == 1) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentValue = when (chartType) {
                        ChartType.WEIGHT -> "${progressPoints[0].weight}kg"
                        ChartType.REPS -> "${progressPoints[0].reps} reps"
                        ChartType.VOLUME -> "${progressPoints[0].volume.toInt()} volume"
                    }

                    Text(
                        text = "Current ${chartType.name.lowercase().capitalize(Locale.ROOT)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentValue,
                        style = MaterialTheme.typography.headlineMedium,
                        color = primaryColor
                    )
                    Text(
                        text = "Need more workouts for progression data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                // Render the chart for multiple data points
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val usableHeight = height * 0.9f
                    val bottomPadding = height * 0.1f

                    // Draw horizontal grid lines
                    val strokePath = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    // Draw 5 horizontal grid lines
                    for (i in 0..4) {
                        val y = height - bottomPadding - (i * usableHeight / 4)
                        drawLine(
                            color = outlineVariantColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = strokePath
                        )
                    }

                    // Calculate x and y positions for each point
                    val points = progressPoints.mapIndexed { index, point ->
                        val x = width * index / (progressPoints.size - 1)
                        val normalizedValue = (dataPoints[index] - minValue) / range
                        val y = height - bottomPadding - (normalizedValue * usableHeight) * animationProgress.value
                        Offset(x, y)
                    }

                    // Draw connecting lines between points
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = primaryColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw points
                    points.forEach { point ->
                        drawCircle(
                            color = primaryColor,
                            radius = 5f,
                            center = point
                        )
                    }
                }
            }

            // Draw the date labels at the bottom
            if (progressPoints.size >= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Show first date
                    Text(
                        text = dateFormatter.format(Date(progressPoints.first().date)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Show last date
                    Text(
                        text = dateFormatter.format(Date(progressPoints.last().date)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (progressPoints.size == 1) {
                // Show a single date at the bottom
                Text(
                    text = dateFormatter.format(Date(progressPoints.first().date)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        }
    }
}

/**
 * Types of charts that can be displayed
 */
enum class ChartType {
    WEIGHT, REPS, VOLUME
}
