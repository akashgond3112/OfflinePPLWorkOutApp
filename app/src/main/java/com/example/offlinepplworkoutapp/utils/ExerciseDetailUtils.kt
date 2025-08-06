package com.example.offlinepplworkoutapp.utils

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.offlinepplworkoutapp.ui.theme.AmberAccent
import com.example.offlinepplworkoutapp.ui.theme.CardBackground
import com.example.offlinepplworkoutapp.ui.theme.SuccessGreen
import com.example.offlinepplworkoutapp.ui.theme.TextSecondary

object ExerciseDetailUtils {

    /**
     * Formats time in seconds to HH:MM:SS or MM:SS format
     */
    fun formatTime(timeInSeconds: Long): String {
        val hours = timeInSeconds / 3600
        val minutes = (timeInSeconds % 3600) / 60
        val seconds = timeInSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Gets appropriate card colors based on set state
     */
    @Composable
    fun getCardColors(
        isCompleted: Boolean,
        isCurrentSet: Boolean,
        isActive: Boolean,
        isLocked: Boolean
    ) = when {
        isCompleted -> CardDefaults.cardColors(
            containerColor = SuccessGreen.copy(alpha = 0.1f)
        )

        isCurrentSet -> CardDefaults.cardColors(
            containerColor = Color.Transparent
        )

        isActive -> CardDefaults.cardColors(
            containerColor = AmberAccent.copy(alpha = 0.1f)
        )

        isLocked -> CardDefaults.cardColors(
            containerColor = TextSecondary.copy(alpha = 0.05f)
        )

        else -> CardDefaults.cardColors(
            containerColor = CardBackground
        )
    }

    /**
     * Gets animated elevation based on set state
     */
    @Composable
    fun getAnimatedElevation(
        isCurrentSet: Boolean,
        isActive: Boolean
    ): Dp {
        val animatedElevation by animateDpAsState(
            targetValue = when {
                isCurrentSet -> 12.dp
                isActive -> 6.dp
                else -> 2.dp
            },
            animationSpec = tween(300)
        )
        return animatedElevation
    }

    /**
     * Formats performance data display string
     */
    fun formatPerformanceData(
        repsPerformed: Int,
        weightUsed: Float
    ): String = buildString {
        if (repsPerformed > 0) append("$repsPerformed reps")
        if (repsPerformed > 0 && weightUsed > 0f) append(" × ")
        if (weightUsed > 0f) append("${weightUsed}lbs")
    }

    /**
     * Determines if performance data should be displayed
     */
    fun shouldShowPerformanceData(
        repsPerformed: Int,
        weightUsed: Float
    ): Boolean = repsPerformed > 0 || weightUsed > 0f

    /**
     * Gets the appropriate status text for a set
     */
    fun getSetStatusText(
        isCompleted: Boolean,
        isLocked: Boolean,
        isCurrentSet: Boolean
    ): String = when {
        isCompleted -> "Set Completed"
        isLocked -> "Complete previous sets first"
        isCurrentSet -> "Active Set"
        else -> "Ready to Start"
    }

    /**
     * Validates if a new set can be added
     */
    fun canAddNewSet(currentSetCount: Int, maxSets: Int = 8): Boolean {
        return currentSetCount < maxSets
    }

    /**
     * Gets help text for set management
     */
    fun getSetManagementHelpText(
        currentSetCount: Int,
        maxSets: Int = 8
    ): String = when {
        currentSetCount >= maxSets -> "Maximum sets reached."
        currentSetCount >= maxSets - 2 -> "Approaching maximum sets ($maxSets)."
        else -> "Maximum $maxSets sets per exercise."
    }
}