package com.example.offlinepplworkoutapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.utils.WorkoutTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DailyWorkoutViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Timer functionality
    private val workoutTimer = WorkoutTimer()

    val timerSeconds = workoutTimer.elapsedSeconds
    val isTimerRunning = workoutTimer.isTimerRunning

    private val _todaysWorkout = MutableStateFlow<List<WorkoutEntryWithExercise>>(emptyList())
    val todaysWorkout: StateFlow<List<WorkoutEntryWithExercise>> = _todaysWorkout.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _completionProgress = MutableStateFlow(0f)
    val completionProgress: StateFlow<Float> = _completionProgress.asStateFlow()

    // Debug mode: allows overriding the current date for testing
    private val _debugDate = MutableStateFlow<String?>(null)
    val debugDate: StateFlow<String?> = _debugDate.asStateFlow()

    // Template-related state
    private val _availableTemplates = MutableStateFlow<List<com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate>>(emptyList())
    val availableTemplates: StateFlow<List<com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate>> = _availableTemplates.asStateFlow()

    private val _selectedTemplate = MutableStateFlow<com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate?>(null)
    val selectedTemplate: StateFlow<com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate?> = _selectedTemplate.asStateFlow()

    init {
        loadTodaysWorkout()
        loadAvailableTemplates()
    }

    private fun loadTodaysWorkout() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTodaysWorkoutWithoutCreating().collect { exercises ->
                _todaysWorkout.value = exercises
                _isLoading.value = false
                updateCompletionProgress(exercises)
            }
        }
    }

    fun loadWorkoutForDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentDate.value = date
            repository.getWorkoutForDateWithoutCreating(date).collect { exercises ->
                _todaysWorkout.value = exercises
                _isLoading.value = false
                updateCompletionProgress(exercises)
            }
        }
    }

    // New method to create today's workout when user wants to start
    fun createTodaysWorkout() {
        println("🎯 VIEWMODEL: createTodaysWorkout() called")
        viewModelScope.launch {
            _isLoading.value = true
            println("🎯 VIEWMODEL: Starting workout creation process...")

            try {
                // 🔧 FIX: Use debug date if available, otherwise use current date
                val targetDate = _debugDate.value ?: _currentDate.value
                println("🎯 VIEWMODEL: Creating workout for date: $targetDate (debug: ${_debugDate.value != null})")

                // Use the date-specific method instead of the hardcoded today method
                repository.createWorkoutForDate(targetDate).collect { exercises ->
                    println("🎯 VIEWMODEL: Received ${exercises.size} exercises from repository")
                    _todaysWorkout.value = exercises
                    _isLoading.value = false
                    updateCompletionProgress(exercises)
                    println("🎯 VIEWMODEL: Workout creation completed successfully")
                }
            } catch (e: Exception) {
                println("🎯 VIEWMODEL ERROR: Failed to create workout - ${e.message}")

                // If foreign key constraint failed, try to populate exercises and retry
                if (e.message?.contains("FOREIGN KEY constraint failed") == true) {
                    println("🎯 VIEWMODEL: Foreign key error detected, trying to populate exercises...")
                    try {
                        // Use the database to populate exercises
                        val context = kotlinx.coroutines.Dispatchers.Main.immediate
                        kotlinx.coroutines.withContext(context) {
                            // Force populate exercises through the database
                            PPLWorkoutDatabase.forcePopulateExercises()

                            // 🔧 FIX: Retry with the correct date
                            val targetDate = _debugDate.value ?: _currentDate.value
                            repository.createWorkoutForDate(targetDate).collect { exercises ->
                                println("🎯 VIEWMODEL: RETRY - Received ${exercises.size} exercises from repository")
                                _todaysWorkout.value = exercises
                                _isLoading.value = false
                                updateCompletionProgress(exercises)
                                println("🎯 VIEWMODEL: RETRY - Workout creation completed successfully")
                            }
                        }
                    } catch (retryException: Exception) {
                        println("🎯 VIEWMODEL ERROR: Retry also failed - ${retryException.message}")
                        _isLoading.value = false
                    }
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    // Debug function to simulate different days
    fun setDebugDate(date: String?) {
        _debugDate.value = date
        if (date == null) {
            // Reset to today - use the original loadTodaysWorkout function
            _currentDate.value = dateFormat.format(Date())
            loadTodaysWorkout()
        } else {
            // Load specific debug date
            loadWorkoutForDate(date)
        }
    }

    fun updateExercise(entryId: Int, sets: Int, reps: Int, isCompleted: Boolean) {
        println("🎯 VIEWMODEL: updateExercise called - entryId: $entryId, sets: $sets, reps: $reps, isCompleted: $isCompleted")
        viewModelScope.launch {
            repository.updateExerciseDetails(entryId, sets, reps, isCompleted)
            println("🎯 VIEWMODEL: updateExercise completed for entryId: $entryId")
        }
    }

    private fun updateCompletionProgress(exercises: List<WorkoutEntryWithExercise>) {
        if (exercises.isEmpty()) {
            _completionProgress.value = 0f
            return
        }

        val completedCount = exercises.count { it.isCompleted }
        _completionProgress.value = completedCount.toFloat() / exercises.size.toFloat()
    }

    fun getCompletionPercentage(): Int {
        return (_completionProgress.value * 100).toInt()
    }

    fun getCurrentDayName(): String {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(_currentDate.value) ?: Date()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }

    fun getWorkoutTypeName(): String {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(_currentDate.value) ?: Date()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.MONDAY -> "Push Day 1 (Chest, Shoulders, Triceps)"
            Calendar.TUESDAY -> "Pull Day 1 (Back, Biceps)"
            Calendar.WEDNESDAY -> "Legs Day 1 (Quads, Hamstrings, Glutes)"
            Calendar.THURSDAY -> "Push Day 2 (Shoulders, Chest, Triceps)"
            Calendar.FRIDAY -> "Pull Day 2 (Back, Biceps)"
            Calendar.SATURDAY -> "Legs Day 2 (Glutes, Quads, Hamstrings)"
            Calendar.SUNDAY -> "Rest Day"
            else -> "Rest Day"
        }
    }

    // New timer-related functions
    fun startExerciseTimer(exerciseId: Int) {
        // Stop any existing timer first
        stopCurrentTimer()

        // Start new timer for this exercise - timer will auto-update every second
        workoutTimer.startTimer(exerciseId)
    }

    private fun stopCurrentTimer() {
        val currentExerciseId = workoutTimer.getCurrentExerciseId()
        val timeSpent = workoutTimer.stopTimer()

        // Save time to database if there was an active timer
        if (currentExerciseId != null && timeSpent > 0) {
            viewModelScope.launch {
                repository.updateExerciseTime(currentExerciseId, timeSpent)
            }
        }
    }

    fun formatTime(seconds: Int): String = workoutTimer.formatTime(seconds)

    fun saveTotalTimeSpent(totalSeconds: Int) {
        viewModelScope.launch {
            // Save the total workout time when all exercises are completed
            val currentExerciseId = workoutTimer.getCurrentExerciseId()
            if (currentExerciseId != null) {
                repository.updateExerciseTime(currentExerciseId, totalSeconds)
                stopCurrentTimer()
            }
        }
    }

    // Method to refresh today's workout data
    fun refreshTodaysWorkout() {
        println("🔄 VIEWMODEL: refreshTodaysWorkout() called")
        viewModelScope.launch {
            _isLoading.value = true

            // Use debug date if available, otherwise use current date
            val targetDate = _debugDate.value ?: _currentDate.value
            println("🔄 VIEWMODEL: Refreshing workout for date: $targetDate")

            // Fetch latest workout data from repository
            repository.getWorkoutForDateWithoutCreating(targetDate).collect { exercises ->
                _todaysWorkout.value = exercises
                _isLoading.value = false
                updateCompletionProgress(exercises)
                println("🔄 VIEWMODEL: Refreshed workout data, found ${exercises.size} exercises")
            }
        }
    }

    // Force complete refresh - more aggressive reset for cache clearing
    fun forceCompleteRefresh() {
        // Cancel any existing data collection jobs
        viewModelScope.launch {
            // Reset all state variables to initial state
            _isLoading.value = true
            _todaysWorkout.value = emptyList()
            _completionProgress.value = 0f
            _debugDate.value = null
            _currentDate.value = dateFormat.format(Date())

            // Stop any running timers
            stopCurrentTimer()
            workoutTimer.reset()

            // Force garbage collection to clear any cached references
            System.gc()

            // Wait a moment for cleanup
            kotlinx.coroutines.delay(100)

            // Reload today's workout from scratch
            loadTodaysWorkout()
        }
    }

    // ===========================================
    // NEW TEMPLATE-BASED METHODS
    // ===========================================

    /**
     * Create workout from specific template
     */
    private fun createWorkoutFromTemplate(templateId: Int, date: String = dateFormat.format(Date())) {
        println("🎯 VIEWMODEL: createWorkoutFromTemplate() called - templateId: $templateId, date: $date")
        viewModelScope.launch {
            _isLoading.value = true

            try {
                repository.createWorkoutFromTemplate(templateId, date).collect { exercises ->
                    println("🎯 VIEWMODEL: Received ${exercises.size} exercises from template $templateId")
                    _todaysWorkout.value = exercises
                    _isLoading.value = false
                    updateCompletionProgress(exercises)
                }
            } catch (e: Exception) {
                println("🎯 VIEWMODEL ERROR: Failed to create workout from template $templateId - ${e.message}")
                _isLoading.value = false
            }
        }
    }

    /**
     * Load available templates for user selection
     */
    private fun loadAvailableTemplates() {
        viewModelScope.launch {
            repository.getAvailableTemplates().collect { templates ->
                _availableTemplates.value = templates
                println("🎯 VIEWMODEL: Loaded ${templates.size} available templates")
            }
        }
    }

    /**
     * Select a template for workout creation
     */
    fun selectTemplate(template: com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate) {
        _selectedTemplate.value = template
        println("🎯 VIEWMODEL: Selected template: ${template.name}")
    }

    /**
     * Create workout from currently selected template
     */
    fun createWorkoutFromSelectedTemplate() {
        val template = _selectedTemplate.value
        if (template != null) {
            createWorkoutFromTemplate(template.id, _currentDate.value)
        } else {
            println("🎯 VIEWMODEL ERROR: No template selected")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCurrentTimer()
        workoutTimer.onCleared() // Properly clean up timer coroutines
    }
}

class DailyWorkoutViewModelFactory(
    private val repository: WorkoutRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyWorkoutViewModel::class.java)) {
            return DailyWorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
