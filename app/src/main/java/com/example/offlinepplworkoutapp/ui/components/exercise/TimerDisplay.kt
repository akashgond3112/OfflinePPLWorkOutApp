package com.example.offlinepplworkoutapp.ui.components.exercise

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.offlinepplworkoutapp.ui.theme.PrimaryCoral
import com.example.offlinepplworkoutapp.ui.theme.SuccessGreen
import com.example.offlinepplworkoutapp.ui.theme.TextSecondary
import com.example.offlinepplworkoutapp.utils.ExerciseDetailUtils.formatTime
import kotlinx.coroutines.delay

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

    // Proper stopwatch: Start from stored time and count up
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