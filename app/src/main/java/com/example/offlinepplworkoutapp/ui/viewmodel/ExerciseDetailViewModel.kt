package com.example.offlinepplworkoutapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SetTimer(
    val setNumber: Int,
    val elapsedTime: Long = 0L,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val startTime: Long = 0L
)

class ExerciseDetailViewModel(
    private val workoutEntry: WorkoutEntryWithExercise,
    private val repository: WorkoutRepository
) : ViewModel() {

    // Load actual sets from database instead of creating in memory
    private val _setTimers = MutableStateFlow<List<SetTimer>>(emptyList())
    val setTimers: StateFlow<List<SetTimer>> = _setTimers.asStateFlow()

    private val _currentRunningSet = MutableStateFlow<Int?>(null)
    val currentRunningSet: StateFlow<Int?> = _currentRunningSet.asStateFlow()

    private val _activeSetIndex = MutableStateFlow<Int>(0)
    val activeSetIndex: StateFlow<Int> = _activeSetIndex.asStateFlow()

    private val _totalExerciseTime = MutableStateFlow(0L)
    val totalExerciseTime: StateFlow<Long> = _totalExerciseTime.asStateFlow()

    private val _completedSets = MutableStateFlow(0)
    val completedSets: StateFlow<Int> = _completedSets.asStateFlow()

    private val _isExerciseCompleted = MutableStateFlow(false)
    val isExerciseCompleted: StateFlow<Boolean> = _isExerciseCompleted.asStateFlow()

    private var timerJob: Job? = null
    private var currentSetId: Int? = null

    init {
        loadSetsFromDatabase()
    }

    private fun loadSetsFromDatabase() {
        viewModelScope.launch {
            // First, ensure sets exist in database
            val existingSets = repository.getSetsForWorkoutEntrySync(workoutEntry.id)

            if (existingSets.isEmpty()) {
                // Create sets in database if they don't exist
                repository.createSetsForWorkoutEntry(workoutEntry.id, workoutEntry.sets)
            }

            // Now load sets from database and observe changes
            repository.getSetsForWorkoutEntry(workoutEntry.id).collect { dbSets ->
                val setTimers = dbSets.map { setEntry ->
                    SetTimer(
                        setNumber = setEntry.setNumber,
                        elapsedTime = (setEntry.elapsedTimeSeconds * 1000).toLong(),
                        isRunning = false, // Never restore running state from DB
                        isCompleted = setEntry.isCompleted,
                        startTime = 0L
                    )
                }
                _setTimers.value = setTimers
                _completedSets.value = dbSets.count { it.isCompleted }
                _isExerciseCompleted.value = dbSets.all { it.isCompleted } && dbSets.isNotEmpty()
                updateTotalExerciseTime()
            }
        }
    }

    fun startSetTimer(setIndex: Int) {
        // Stop any currently running timer
        stopAllTimers()

        // Start timer for this set
        val currentTime = System.currentTimeMillis()
        val updatedTimers = _setTimers.value.toMutableList()
        updatedTimers[setIndex] = updatedTimers[setIndex].copy(
            isRunning = true,
            startTime = currentTime
        )
        _setTimers.value = updatedTimers
        _currentRunningSet.value = setIndex

        // Store the database set ID for persistence
        viewModelScope.launch {
            val dbSets = repository.getSetsForWorkoutEntrySync(workoutEntry.id)
            currentSetId = dbSets.getOrNull(setIndex)?.id
        }

        // Start the timer coroutine
        timerJob = viewModelScope.launch {
            while (_setTimers.value.getOrNull(setIndex)?.isRunning == true) {
                delay(1000) // Update every second
                updateTimerForSet(setIndex)
            }
        }
    }

    fun stopSetTimer(setIndex: Int) {
        val updatedTimers = _setTimers.value.toMutableList()
        val timer = updatedTimers.getOrNull(setIndex) ?: return

        if (timer.isRunning) {
            val finalElapsedTime = timer.elapsedTime + (System.currentTimeMillis() - timer.startTime)

            updatedTimers[setIndex] = timer.copy(
                isRunning = false,
                elapsedTime = finalElapsedTime,
                isCompleted = true  // Mark as completed when stopping
            )
            _setTimers.value = updatedTimers
            _currentRunningSet.value = null
            timerJob?.cancel()

            // Persist timer to database
            currentSetId?.let { setId ->
                viewModelScope.launch {
                    repository.updateSetProgress(
                        setId = setId,
                        isCompleted = true,  // Mark as completed in the database
                        elapsedTimeSeconds = (finalElapsedTime / 1000).toInt()
                    )

                    // Update exercise completion status
                    repository.updateExerciseCompletionFromSets(workoutEntry.id)

                    // Update local completed sets count
                    val completedCount = _setTimers.value.count { it.isCompleted }
                    _completedSets.value = completedCount

                    // Check if all sets are completed
                    if (completedCount == workoutEntry.sets) {
                        _isExerciseCompleted.value = true
                    } else {
                        // Advance to next set
                        val nextSetIndex = setIndex + 1
                        if (nextSetIndex < _setTimers.value.size && !_setTimers.value[nextSetIndex].isCompleted) {
                            _activeSetIndex.value = nextSetIndex
                        }
                    }
                }
            }

            updateTotalExerciseTime()
        }
    }

    fun completeSet(setIndex: Int) {
        val updatedTimers = _setTimers.value.toMutableList()
        val timer = updatedTimers.getOrNull(setIndex) ?: return

        updatedTimers[setIndex] = timer.copy(
            isCompleted = true,
            isRunning = false
        )
        _setTimers.value = updatedTimers
        _currentRunningSet.value = null
        timerJob?.cancel()

        // Persist completion to database
        viewModelScope.launch {
            val dbSets = repository.getSetsForWorkoutEntrySync(workoutEntry.id)
            val setEntry = dbSets.getOrNull(setIndex)

            setEntry?.let {
                repository.updateSetProgress(
                    setId = it.id,
                    isCompleted = true,
                    elapsedTimeSeconds = (timer.elapsedTime / 1000).toInt()
                )

                // Check if all sets are completed and update exercise
                repository.updateExerciseCompletionFromSets(workoutEntry.id)
            }
        }

        // Update local state
        val completedCount = updatedTimers.count { it.isCompleted }
        _completedSets.value = completedCount

        // Check if all sets are completed
        if (completedCount == workoutEntry.sets) {
            _isExerciseCompleted.value = true
        }

        updateTotalExerciseTime()
    }

    private fun stopAllTimers() {
        timerJob?.cancel()
        val updatedTimers = _setTimers.value.map { timer ->
            timer.copy(isRunning = false)
        }
        _setTimers.value = updatedTimers
        _currentRunningSet.value = null
    }

    private fun updateTimerForSet(setIndex: Int) {
        val updatedTimers = _setTimers.value.toMutableList()
        val timer = updatedTimers[setIndex]

        if (timer.isRunning) {
            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - timer.startTime + timer.elapsedTime
            updatedTimers[setIndex] = timer.copy(elapsedTime = elapsed)
            _setTimers.value = updatedTimers
        }
    }

    private fun updateTotalExerciseTime() {
        val totalTime = _setTimers.value.sumOf { it.elapsedTime }
        _totalExerciseTime.value = totalTime
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class ExerciseDetailViewModelFactory(
    private val workoutEntry: WorkoutEntryWithExercise,
    private val repository: WorkoutRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseDetailViewModel::class.java)) {
            return ExerciseDetailViewModel(workoutEntry, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
