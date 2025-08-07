package com.example.offlinepplworkoutapp.ui.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.entity.SetEntry
import com.example.offlinepplworkoutapp.data.entity.WorkoutDay
import com.example.offlinepplworkoutapp.data.history.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * State class representing the UI state of the history screen
 */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentDate: String = "",
    val workoutType: String = "",
    val exercises: List<WorkoutEntryWithExercise> = emptyList(),
    val hasPreviousWorkout: Boolean = false,
    val hasNextWorkout: Boolean = false,
    val totalWorkoutTime: Int = 0,
    val completedSets: Int = 0,
    val totalSets: Int = 0
)

/**
 * ViewModel for managing workout history data and state
 */
class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    // Current UI state for the history screen
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // Set data for selected exercise
    private val _selectedExerciseSets = MutableStateFlow<Map<Int, List<SetEntry>>>(emptyMap())
    val selectedExerciseSets: StateFlow<Map<Int, List<SetEntry>>> = _selectedExerciseSets.asStateFlow()

    private var currentWorkoutDay: WorkoutDay? = null

    init {
        loadMostRecentWorkout()
    }

    /**
     * Load the most recent workout
     */
    fun loadMostRecentWorkout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get the most recent workout day
                val mostRecent = historyRepository.getMostRecentWorkoutDay()
                if (mostRecent != null) {
                    currentWorkoutDay = mostRecent
                    loadWorkoutForDate(mostRecent.date)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No workout history found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading workout history: ${e.message}"
                )
            }
        }
    }

    /**
     * Load workout data for a specific date
     */
    fun loadWorkoutForDate(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get the workout day for this date
                val workoutDay = historyRepository.getWorkoutDayByDate(date)

                if (workoutDay != null) {
                    currentWorkoutDay = workoutDay

                    // Check for previous/next workouts
                    val hasPrevious = historyRepository.hasPreviousWorkout(date)
                    val hasNext = historyRepository.hasNextWorkout(date)

                    // Format date for display
                    val displayDate = try {
                        val parsedDate = dateFormat.parse(date)
                        if (parsedDate != null) displayDateFormat.format(parsedDate) else date
                    } catch (e: Exception) {
                        date
                    }

                    // Load workout entries
                    historyRepository.getWorkoutEntriesForDate(date).collect { exercises ->
                        // Calculate stats
                        val totalTime = historyRepository.calculateTotalWorkoutTime(workoutDay.id)
                        val (completed, total) = historyRepository.calculateSetsCompletion(workoutDay.id)

                        // Determine workout type from day of week
                        val workoutType = determineWorkoutTypeFromDate(date)

                        // Load sets for all exercises
                        val exerciseSets = mutableMapOf<Int, List<SetEntry>>()
                        exercises.forEach { exercise ->
                            val sets = historyRepository.getSetsForWorkoutEntrySync(exercise.id)
                            exerciseSets[exercise.id] = sets
                        }

                        // Update UI state
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            currentDate = displayDate,
                            workoutType = workoutType,
                            exercises = exercises,
                            hasPreviousWorkout = hasPrevious,
                            hasNextWorkout = hasNext,
                            totalWorkoutTime = totalTime,
                            completedSets = completed,
                            totalSets = total
                        )

                        _selectedExerciseSets.value = exerciseSets
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No workout found for date $date"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading workout: ${e.message}"
                )
            }
        }
    }

    /**
     * Determine workout type (Push/Pull/Legs) from the date
     */
    private fun determineWorkoutTypeFromDate(date: String): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = dateFormat.parse(date) ?: Date()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            when (dayOfWeek) {
                Calendar.MONDAY, Calendar.THURSDAY -> "Push Day"
                Calendar.TUESDAY, Calendar.FRIDAY -> "Pull Day"
                Calendar.WEDNESDAY, Calendar.SATURDAY -> "Legs Day"
                else -> "Rest Day"
            }
        } catch (e: Exception) {
            "Workout"
        }
    }

    /**
     * Navigate to the previous workout
     */
    fun loadPreviousWorkout() {
        viewModelScope.launch {
            val currentDate = currentWorkoutDay?.date ?: return@launch
            val previousDate = historyRepository.getPreviousWorkoutDate(currentDate)

            if (previousDate != null) {
                loadWorkoutForDate(previousDate)
            }
        }
    }

    /**
     * Navigate to the next workout
     */
    fun loadNextWorkout() {
        viewModelScope.launch {
            val currentDate = currentWorkoutDay?.date ?: return@launch
            val nextDate = historyRepository.getNextWorkoutDate(currentDate)

            if (nextDate != null) {
                loadWorkoutForDate(nextDate)
            }
        }
    }

    /**
     * Format workout time in minutes and seconds
     */
    fun formatWorkoutTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    /**
     * Factory for creating HistoryViewModel instances with proper dependencies
     */
    class Factory(private val database: PPLWorkoutDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                val historyRepository = HistoryRepository(
                    database.workoutDayDao(),
                    database.workoutEntryDao(),
                    database.setEntryDao()
                )
                return HistoryViewModel(historyRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}