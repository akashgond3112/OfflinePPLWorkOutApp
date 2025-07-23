package com.example.offlinepplworkoutapp.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkoutTimer {
    private var startTime: Long = 0
    private var isRunning = false
    private var currentExerciseId: Int? = null

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    fun startTimer(exerciseId: Int) {
        if (!isRunning || currentExerciseId != exerciseId) {
            startTime = System.currentTimeMillis()
            isRunning = true
            currentExerciseId = exerciseId
            _isTimerRunning.value = true
        }
    }

    fun stopTimer(): Int {
        if (isRunning) {
            val totalSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            isRunning = false
            currentExerciseId = null
            _isTimerRunning.value = false
            _elapsedSeconds.value = 0
            return totalSeconds
        }
        return 0
    }

    fun updateElapsedTime() {
        if (isRunning) {
            val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            _elapsedSeconds.value = elapsed
        }
    }

    fun getCurrentExerciseId(): Int? = currentExerciseId

    fun reset() {
        isRunning = false
        currentExerciseId = null
        _isTimerRunning.value = false
        _elapsedSeconds.value = 0
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
}
