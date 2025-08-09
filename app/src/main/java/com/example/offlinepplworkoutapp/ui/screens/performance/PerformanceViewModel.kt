package com.example.offlinepplworkoutapp.ui.screens.performance

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.performance.ExercisePerformance
import com.example.offlinepplworkoutapp.data.performance.PerformanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Performance Tab screen
 */
@RequiresApi(Build.VERSION_CODES.O)
class PerformanceViewModel(private val repository: PerformanceRepository) : ViewModel() {

    // UI state for the Performance screen
    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    init {
        // Load initial data with default time period (7 days)
        loadPerformanceData(7)
    }

    /**
     * Load performance data for the specified time period
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPerformanceData(days: Int) {
        viewModelScope.launch {
            // Update the time period in the UI state
            _uiState.update { it.copy(selectedTimePeriod = days) }

            // Show loading state
            _uiState.update { it.copy(isLoading = true) }

            // Collect performance data from repository
            repository.getAllExercisesPerformance(days).collect { performanceList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        exercisePerformances = performanceList,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Select a muscle group to display
     */
    fun selectMuscle(muscleName: String) {
        _uiState.update { currentState ->
            // Clear expanded exercises when changing muscle groups
            currentState.copy(
                selectedMuscle = muscleName,
                expandedExercises = emptySet()
            )
        }
    }

    /**
     * Clear the selected muscle group and return to muscle group view
     */
    fun clearSelectedMuscle() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedMuscle = null,
                expandedExercises = emptySet()
            )
        }
    }

    /**
     * Toggle the expanded state of an exercise
     */
    fun toggleExerciseExpanded(exerciseId: Int) {
        _uiState.update { currentState ->
            val expandedExercises = currentState.expandedExercises.toMutableSet()

            if (expandedExercises.contains(exerciseId)) {
                expandedExercises.remove(exerciseId)
            } else {
                // Collapse all other exercises (since we only show one at a time)
                expandedExercises.clear()
                expandedExercises.add(exerciseId)
            }

            currentState.copy(expandedExercises = expandedExercises)
        }
    }

    /**
     * Factory for creating the ViewModel
     */
    class Factory(private val database: PPLWorkoutDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerformanceViewModel::class.java)) {
                val repository = PerformanceRepository(database)
                return PerformanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * UI state for the Performance screen
 */
data class PerformanceUiState(
    val exercisePerformances: List<ExercisePerformance> = emptyList(),
    val expandedExercises: Set<Int> = emptySet(),
    val selectedTimePeriod: Int = 7,
    val selectedMuscle: String? = null,
    val isLoading: Boolean = false
)
