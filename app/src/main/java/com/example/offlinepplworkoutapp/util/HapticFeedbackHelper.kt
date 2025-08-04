package com.example.offlinepplworkoutapp.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Helper class for providing haptic feedback throughout the app
 */
class HapticFeedbackHelper(private val context: Context) {

    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * Different haptic feedback types for different actions
     */
    enum class FeedbackType {
        BUTTON_PRESS,       // Light click for normal button presses
        SUCCESS,            // Medium click with pattern for success actions (like completing a set)
        ERROR,              // Error pattern (like validation error)
        TIMER_START_STOP,   // Special pattern for timer start/stop
        HEAVY_CLICK         // Strong click for important actions
    }

    /**
     * Perform haptic feedback based on the feedback type
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun performHapticFeedback(feedbackType: FeedbackType) {
        when (feedbackType) {
            FeedbackType.BUTTON_PRESS -> {
                // Simple click for buttons
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20)
                }
            }
            FeedbackType.SUCCESS -> {
                // Double click pattern for success
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 40, 60, 40), -1)
                }
            }
            FeedbackType.ERROR -> {
                // Error pattern (longer vibration)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80)
                }
            }
            FeedbackType.TIMER_START_STOP -> {
                // Special pattern for timer actions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 30, 50, 30), -1)
                }
            }
            FeedbackType.HEAVY_CLICK -> {
                // Strong click
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        }
    }

    /**
     * Alternative method using View for haptic feedback
     * This may work better in some cases where the vibrator service has limitations
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun performViewHapticFeedback(view: View, feedbackType: FeedbackType) {
        when (feedbackType) {
            FeedbackType.BUTTON_PRESS -> {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            FeedbackType.SUCCESS -> {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
            FeedbackType.ERROR -> {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            }
            FeedbackType.TIMER_START_STOP -> {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            FeedbackType.HEAVY_CLICK -> {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
        }
    }
}

/**
 * Composable function to create and remember a HapticFeedbackHelper instance
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackHelper {
    val context = LocalContext.current
    return remember { HapticFeedbackHelper(context) }
}

/**
 * Extension function for View to perform haptic feedback
 */
@RequiresApi(Build.VERSION_CODES.R)
fun View.performCustomHapticFeedback(feedbackType: HapticFeedbackHelper.FeedbackType) {
    val hapticHelper = HapticFeedbackHelper(this.context)
    hapticHelper.performViewHapticFeedback(this, feedbackType)
}
