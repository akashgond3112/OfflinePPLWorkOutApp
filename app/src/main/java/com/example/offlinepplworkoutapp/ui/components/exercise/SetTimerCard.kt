package com.example.offlinepplworkoutapp.ui.components.exercise

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinepplworkoutapp.ui.theme.ProgressEnd
import com.example.offlinepplworkoutapp.ui.theme.ProgressStart
import com.example.offlinepplworkoutapp.utils.HapticFeedbackHelper
import com.example.offlinepplworkoutapp.utils.rememberHapticFeedback

@Composable
fun SetTimerCard(
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
    onEditSet: () -> Unit,
    onDeleteSet: (() -> Unit)? = null,
    onResetSet: (() -> Unit)? = null // New reset parameter
) {
    val hapticFeedback = rememberHapticFeedback()

    // Animation for card elevation based on state
    val animatedElevation by animateDpAsState(
        targetValue = when {
            isCurrentSet -> 16.dp
            isActive -> 8.dp
            else -> 4.dp
        },
        animationSpec = tween(300)
    )

    // Card colors based on state
    val cardColors = when {
        isCompleted -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )

        isCurrentSet -> CardDefaults.cardColors(
            containerColor = Color.Transparent
        )

        isActive -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )

        isLocked -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
        )

        else -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
            // Set header with status icon and timer
            SetHeaderRow(
                setNumber = setNumber,
                totalSets = totalSets,
                setTimer = setTimer,
                isCurrentSet = isCurrentSet,
                isCompleted = isCompleted,
                isLocked = isLocked,
                onDeleteSet = onDeleteSet
            )

            // Target reps and performance data
            SetInfoRow(
                targetReps = targetReps,
                repsPerformed = repsPerformed,
                weightUsed = weightUsed,
                isCompleted = isCompleted
            )

            // Action buttons
            SetActionButtons(
                isCompleted = isCompleted,
                isLocked = isLocked,
                isCurrentSet = isCurrentSet,
                repsPerformed = repsPerformed,
                weightUsed = weightUsed,
                onStartTimer = onStartTimer,
                onStopTimer = onStopTimer,
                onEditSet = onEditSet,
                onResetSet = onResetSet, // Pass reset action
                hapticFeedback = hapticFeedback
            )
        }
    }
}

@Composable
private fun SetHeaderRow(
    setNumber: Int,
    totalSets: Int,
    setTimer: Long,
    isCurrentSet: Boolean,
    isCompleted: Boolean,
    isLocked: Boolean,
    onDeleteSet: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Set status icon
            SetStatusIcon(
                isCompleted = isCompleted,
                isCurrentSet = isCurrentSet,
                isLocked = isLocked
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Set $setNumber of $totalSets",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Delete button for incomplete sets
            if (onDeleteSet != null) {
                IconButton(
                    onClick = onDeleteSet,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete set",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Timer display
            TimerDisplay(
                time = setTimer,
                isActive = isCurrentSet,
                isCompleted = isCompleted
            )
        }
    }
}

@Composable
private fun SetStatusIcon(
    isCompleted: Boolean,
    isCurrentSet: Boolean,
    isLocked: Boolean
) {
    when {
        isCompleted -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        isCurrentSet -> {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Active",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        isLocked -> {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        else -> {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Ready",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SetInfoRow(
    targetReps: Int,
    repsPerformed: Int,
    weightUsed: Float,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Target: $targetReps reps",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        )

        // Display performance data when set is completed
        if (isCompleted && (repsPerformed > 0 || weightUsed > 0f)) {
            Text(
                text = buildString {
                    if (repsPerformed > 0) append("$repsPerformed reps")
                    if (repsPerformed > 0 && weightUsed > 0f) append(" × ")
                    if (weightUsed > 0f) append("${weightUsed}lbs")
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun SetActionButtons(
    isCompleted: Boolean,
    isLocked: Boolean,
    isCurrentSet: Boolean,
    repsPerformed: Int,
    weightUsed: Float,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onEditSet: () -> Unit,
    onResetSet: (() -> Unit)?, // New reset action
    hapticFeedback: HapticFeedbackHelper
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            isCompleted -> {
                SetCompletedState(
                    repsPerformed = repsPerformed,
                    weightUsed = weightUsed,
                    onEditSet = onEditSet,
                    hapticFeedback = hapticFeedback,
                    onResetSet = onResetSet // Pass the reset callback
                )
            }

            isLocked -> {
                Text(
                    text = "Complete previous sets first",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            isCurrentSet -> {
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.SUCCESS)
                        onStopTimer()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
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
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.TIMER_START_STOP)
                        onStartTimer()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
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

        // Reset button for incomplete sets
        if (onResetSet != null && !isCompleted) {
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.RESET)
                    onResetSet()
                },
                modifier = Modifier.size(40.dp),
                enabled = !isLocked
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset set",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SetCompletedState(
    repsPerformed: Int,
    weightUsed: Float,
    onEditSet: () -> Unit,
    hapticFeedback: HapticFeedbackHelper,
    onResetSet: (() -> Unit)? = null  // Add reset parameter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Set Completed",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Detailed performance data display
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Row for action buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Reset button for completed sets
            if (onResetSet != null) {
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.RESET)
                        onResetSet()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset set",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Edit button for completed sets
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackHelper.FeedbackType.BUTTON_PRESS)
                    onEditSet()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit set data",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}