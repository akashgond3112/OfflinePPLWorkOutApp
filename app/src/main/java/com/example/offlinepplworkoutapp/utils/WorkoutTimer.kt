package com.example.offlinepplworkoutapp.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkoutTimer {
    private var startTime: Long = 0
    private var currentExerciseId: Int? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    fun startTimer(exerciseId: Int) {
        // Stop any existing timer first
        stopTimerInternal()

        startTime = System.currentTimeMillis()
        currentExerciseId = exerciseId
        _isTimerRunning.value = true

        startTimerUpdates()
    }

    private fun startTimerUpdates() {
        timerJob = scope.launch {
            try {
                while (isActive && _isTimerRunning.value) {
                    val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                    _elapsedSeconds.value = elapsed

                    // Simple 1-second delay for accurate counting
                    delay(1000)
                }
            } catch (e: CancellationException) {
                // Timer was cancelled, this is expected
            }
        }
    }

    fun stopTimer(): Int {
        val totalSeconds = if (_isTimerRunning.value) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }

        stopTimerInternal()
        return totalSeconds
    }

    private fun stopTimerInternal() {
        timerJob?.cancel()
        timerJob = null
        currentExerciseId = null
        _isTimerRunning.value = false
        _elapsedSeconds.value = 0
    }

    fun pauseTimer(): Int {
        val totalSeconds = if (_isTimerRunning.value) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }

        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false

        return totalSeconds
    }

    fun resumeTimer() {
        if (currentExerciseId != null && !_isTimerRunning.value) {
            // Adjust start time to account for the pause
            val pausedSeconds = _elapsedSeconds.value
            startTime = System.currentTimeMillis() - (pausedSeconds * 1000)
            _isTimerRunning.value = true
            startTimerUpdates()
        }
    }

    fun updateElapsedTime() {
        if (_isTimerRunning.value) {
            val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            _elapsedSeconds.value = elapsed
        }
    }

    fun getCurrentExerciseId(): Int? = currentExerciseId

    fun reset() {
        stopTimerInternal()
    }

    fun onCleared() {
        timerJob?.cancel()
        scope.cancel()
    }

    /**
     * 🔧 FIXED: Proper HH:MM:SS format with proper hour handling
     */
    fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }
}