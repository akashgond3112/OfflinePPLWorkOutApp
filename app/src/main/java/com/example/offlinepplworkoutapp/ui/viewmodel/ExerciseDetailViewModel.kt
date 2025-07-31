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

    // 🚀 FIXED: Enhanced rest timer functionality with proper time tracking
    private val _restTimer = MutableStateFlow(0L)
    val restTimer: StateFlow<Long> = _restTimer.asStateFlow()

    private val _isRestActive = MutableStateFlow(false)
    val isRestActive: StateFlow<Boolean> = _isRestActive.asStateFlow()

    // 🔧 NEW: Track accumulated rest time for total exercise time calculation
    private val _totalRestTime = MutableStateFlow(0L)
    val totalRestTime: StateFlow<Long> = _totalRestTime.asStateFlow()

    // 🚀 NEW: Phase 2.1.2 - Set data entry dialog state
    private val _showSetDataDialog = MutableStateFlow(false)
    val showSetDataDialog: StateFlow<Boolean> = _showSetDataDialog.asStateFlow()

    private val _pendingSetData = MutableStateFlow<Pair<Int, Int>?>(null) // (setIndex, setId)
    val pendingSetData: StateFlow<Pair<Int, Int>?> = _pendingSetData.asStateFlow()

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null
    private var currentSetId: Int? = null

    // 🔧 NEW: Track current rest session start time
    private var restStartTime: Long = 0L

    init {
        loadSetsFromDatabase()
    }

    private fun loadSetsFromDatabase() {
        viewModelScope.launch {
            println("🔍 DETAIL VM: Loading sets for workout entry ID: ${workoutEntry.id}")
            println("🔍 DETAIL VM: Exercise: '${workoutEntry.exerciseName}' (Exercise ID: ${workoutEntry.exerciseId})")

            // First, ensure sets exist in database
            val existingSets = repository.getSetsForWorkoutEntrySync(workoutEntry.id)
            println("🔍 DETAIL VM: Found ${existingSets.size} existing sets in database")

            if (existingSets.isEmpty()) {
                println("🔍 DETAIL VM: No sets found, creating ${workoutEntry.sets} sets")
                // Create sets in database if they don't exist
                repository.createSetsForWorkoutEntry(workoutEntry.id, workoutEntry.sets)
                println("🔍 DETAIL VM: Sets created, reloading...")
            } else {
                println("🔍 DETAIL VM: Sets already exist:")
                existingSets.forEach { set ->
                    println("🔍 DETAIL VM: Set ID=${set.id}, SetNumber=${set.setNumber}, WorkoutEntryId=${set.workoutEntryId}, completed=${set.isCompleted}, time=${set.elapsedTimeSeconds}s")
                }
            }

            // Now load sets from database and observe changes
            repository.getSetsForWorkoutEntry(workoutEntry.id).collect { dbSets ->
                println("🔍 DETAIL VM: Received ${dbSets.size} sets from Flow for WorkoutEntry ID: ${workoutEntry.id}")
                dbSets.forEach { set ->
                    println("🔍 DETAIL VM: Flow Set ID=${set.id}, SetNumber=${set.setNumber}, WorkoutEntryId=${set.workoutEntryId}, completed=${set.isCompleted}, time=${set.elapsedTimeSeconds}s")
                }

                val setTimers = dbSets.map { setEntry ->
                    SetTimer(
                        setNumber = setEntry.setNumber,
                        elapsedTime = (setEntry.elapsedTimeSeconds * 1000).toLong(),
                        isRunning = false, // Never restore running state from DB
                        isCompleted = setEntry.isCompleted,
                        startTime = 0L
                    )
                }

                println("🔍 DETAIL VM: Created ${setTimers.size} SetTimer objects for '${workoutEntry.exerciseName}'")
                _setTimers.value = setTimers
                _completedSets.value = dbSets.count { it.isCompleted }
                _isExerciseCompleted.value = dbSets.all { it.isCompleted } && dbSets.isNotEmpty()

                // 🔧 FIX: Set the active set index to the first incomplete set
                val firstIncompleteSetIndex = setTimers.indexOfFirst { !it.isCompleted }
                _activeSetIndex.value =
                    if (firstIncompleteSetIndex != -1) firstIncompleteSetIndex else 0
                println("🔍 DETAIL VM: Set active set index to: ${_activeSetIndex.value}")

                updateTotalExerciseTime()

                println("🔍 DETAIL VM: State updated for '${workoutEntry.exerciseName}' - completedSets: ${_completedSets.value}, isCompleted: ${_isExerciseCompleted.value}")
            }
        }
    }

    fun startSetTimer(setIndex: Int) {
        println("🚀 REST DEBUG: startSetTimer called for set ${setIndex + 1}")

        // Stop any currently running timer
        stopAllTimers()

        // 🚀 NEW: Stop rest timer when starting a new set
        println("🚀 REST DEBUG: Stopping rest timer before starting new set")
        stopRestTimer()

        // Start timer for this set
        val currentTime = System.currentTimeMillis()
        val updatedTimers = _setTimers.value.toMutableList()
        updatedTimers[setIndex] = updatedTimers[setIndex].copy(
            isRunning = true,
            startTime = currentTime
        )
        _setTimers.value = updatedTimers
        _currentRunningSet.value = setIndex

        println("🚀 REST DEBUG: Set ${setIndex + 1} timer started at $currentTime")

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
            // 🔧 FIXED: Use current elapsed time directly, don't double-count
            val finalElapsedTime = System.currentTimeMillis() - timer.startTime

            println("🛑 STOP TIMER DEBUG: Set ${setIndex + 1}")
            println("🛑 STOP TIMER DEBUG: timer.elapsedTime = ${timer.elapsedTime}ms")
            println("🛑 STOP TIMER DEBUG: startTime = ${timer.startTime}")
            println("🛑 STOP TIMER DEBUG: currentTime = ${System.currentTimeMillis()}")
            println("🛑 STOP TIMER DEBUG: calculated elapsed = ${finalElapsedTime}ms (${finalElapsedTime / 1000}s)")

            updatedTimers[setIndex] = timer.copy(
                isRunning = false,
                elapsedTime = finalElapsedTime
                // 🚀 CHANGED: Don't mark as completed here - wait for user data entry
            )
            _setTimers.value = updatedTimers
            _currentRunningSet.value = null
            timerJob?.cancel()

            // 🚀 NEW: Phase 2.1.2 - Show data entry dialog instead of immediate completion
            currentSetId?.let { setId ->
                println("🎯 DIALOG: Showing set data entry dialog for set ${setIndex + 1}")
                _pendingSetData.value = Pair(setIndex, setId)
                _showSetDataDialog.value = true

                // 🚀 IMPORTANT: Start rest timer while user enters data
                startRestTimer()
            }
        }
    }

    // 🚀 NEW: Phase 2.1.2 - Handle set performance data submission
    fun submitSetPerformanceData(repsPerformed: Int, weightUsed: Float) {
        val pendingData = _pendingSetData.value ?: return
        val (setIndex, setId) = pendingData

        println("🎯 DIALOG: Submitting performance data - Set ${setIndex + 1}, Reps: $repsPerformed, Weight: $weightUsed")

        viewModelScope.launch {
            // Get the timer data for this set
            val timer = _setTimers.value.getOrNull(setIndex)
            val elapsedTimeSeconds = ((timer?.elapsedTime ?: 0L) / 1000).toInt()

            // Update database with completion and performance data
            repository.updateSetProgressWithPerformanceData(
                setId = setId,
                isCompleted = true,
                elapsedTimeSeconds = elapsedTimeSeconds,
                repsPerformed = repsPerformed,
                weightUsed = weightUsed
            )

            // Update local state to mark set as completed
            val updatedTimers = _setTimers.value.toMutableList()
            updatedTimers[setIndex] = updatedTimers[setIndex].copy(isCompleted = true)
            _setTimers.value = updatedTimers

            // Update exercise completion status
            repository.updateExerciseCompletionFromSets(workoutEntry.id)

            // Update local completed sets count
            val completedCount = _setTimers.value.count { it.isCompleted }
            _completedSets.value = completedCount

            // Check if all sets are completed
            if (completedCount == workoutEntry.sets) {
                _isExerciseCompleted.value = true
                // Stop rest timer if all sets are completed
                stopRestTimer()
            } else {
                // Advance to next set - find the first incomplete set
                val nextIncompleteSetIndex = _setTimers.value.indexOfFirst { !it.isCompleted }
                if (nextIncompleteSetIndex != -1) {
                    _activeSetIndex.value = nextIncompleteSetIndex
                    println("🔍 DETAIL VM: Advanced to next set index: $nextIncompleteSetIndex")
                }
            }

            // Update total time calculation
            updateTotalExerciseTime()

            // Hide dialog and clear pending data
            _showSetDataDialog.value = false
            _pendingSetData.value = null

            println("🎯 DIALOG: Set performance data saved successfully")
        }
    }

    // 🚀 NEW: Phase 2.1.2 - Cancel dialog (if needed for future enhancements)
    fun dismissSetDataDialog() {
        // Note: As per requirements, there should be no cancel option
        // This method is for potential future use or error handling
        println("🎯 DIALOG: Dialog dismissed (should not happen in normal flow)")
        _showSetDataDialog.value = false
        _pendingSetData.value = null
    }

    // 🚀 NEW: Rest timer functionality
    private fun startRestTimer() {
        println("🚀 REST DEBUG: startRestTimer() called")
        println("🚀 REST DEBUG: Current _isRestActive state: ${_isRestActive.value}")

        // Don't start a new rest timer if one is already active
        if (_isRestActive.value) {
            println("🚀 REST DEBUG: Rest timer already active, skipping start")
            return
        }

        println("🚀 REST DEBUG: Setting _isRestActive to true")
        _isRestActive.value = true
        val restStartTime = System.currentTimeMillis()
        println("🚀 REST DEBUG: Rest timer start time: $restStartTime")

        // Start the rest timer job
        restTimerJob = viewModelScope.launch {
            var elapsedRestTime = 0L
            println("🚀 REST DEBUG: Rest timer coroutine started")

            while (_isRestActive.value) {
                delay(1000) // Update every second
                elapsedRestTime += 1000
                _restTimer.value = elapsedRestTime

                println("⏱️ REST TIMER: ${elapsedRestTime / 1000}s (Live) - _restTimer.value = ${_restTimer.value}")
            }
            println("🚀 REST DEBUG: Rest timer coroutine ended")
        }

        println("🚀 REST TIMER STARTED - Job created: ${restTimerJob != null}")
    }

    private fun stopRestTimer() {
        println("🚀 REST DEBUG: stopRestTimer() called")
        println("🚀 REST DEBUG: Current _isRestActive state: ${_isRestActive.value}")

        if (!_isRestActive.value) {
            println("🚀 REST DEBUG: Rest timer not active, nothing to stop")
            return
        }

        // 🔧 CRITICAL FIX: Capture rest time before stopping
        val completedRestTime = _restTimer.value
        println("🚀 REST DEBUG: Capturing rest time: ${completedRestTime}ms (${completedRestTime / 1000}s)")

        // 🔧 NEW: Add to total rest time accumulator
        _totalRestTime.value += completedRestTime
        println("🚀 REST DEBUG: Added ${completedRestTime}ms to total rest time")
        println("🚀 REST DEBUG: Total accumulated rest time is now: ${_totalRestTime.value}ms (${_totalRestTime.value / 1000}s)")

        println("🚀 REST DEBUG: Setting _isRestActive to false")
        _isRestActive.value = false

        println("🚀 REST DEBUG: Cancelling rest timer job")
        restTimerJob?.cancel()
        restTimerJob = null

        // Reset rest timer value (but keep the accumulated total)
        val previousValue = _restTimer.value
        _restTimer.value = 0L
        println("🚀 REST DEBUG: Reset _restTimer from ${previousValue}ms to ${_restTimer.value}ms")

        // 🔧 NEW: Update total exercise time with new rest time
        updateTotalExerciseTime()

        println("⏹️ REST TIMER STOPPED - Rest time captured and added to total")
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
            // 🔧 FIXED: Don't add previous elapsedTime - just calculate from start
            val elapsed = currentTime - timer.startTime
            updatedTimers[setIndex] = timer.copy(elapsedTime = elapsed)
            _setTimers.value = updatedTimers

            // 🔧 REMOVED: Don't update total time during live timer updates
            // updateTotalExerciseTime() // This was causing live updates in top bar

            // 🔧 ADDED: Force UI recomposition for live stopwatch display
            println("⏱️ TIMER: Set ${setIndex + 1} - ${elapsed / 1000}s (Live)")
        }
    }

    private fun updateTotalExerciseTime() {
        // 🔧 FIXED: Only count completed sets for total time, not running timers
        val completedSets = _setTimers.value.filter { it.isCompleted }

        println("🕐 TOTAL TIME DEBUG: Calculating total exercise time...")
        println("🕐 TOTAL TIME DEBUG: Found ${completedSets.size} completed sets:")

        completedSets.forEachIndexed { index, set ->
            println("🕐 TOTAL TIME DEBUG: Set ${index + 1}: ${set.elapsedTime}ms (${set.elapsedTime / 1000}s)")
        }

        val setTime = completedSets.sumOf { it.elapsedTime }

        // 🚀 NEW: Include accumulated rest time in total
        val restTime = _totalRestTime.value
        val totalTime = setTime + restTime

        println("🕐 TOTAL TIME DEBUG: Sum of all completed sets: ${setTime}ms (${setTime / 1000}s)")
        println("🕐 TOTAL TIME DEBUG: Total accumulated rest time: ${restTime}ms (${restTime / 1000}s)")
        println("🕐 TOTAL TIME DEBUG: Combined total time: ${totalTime}ms (${totalTime / 1000}s)")
        println("🕐 TOTAL TIME DEBUG: Setting _totalExerciseTime to: ${totalTime}")

        _totalExerciseTime.value = totalTime

        println("🕐 TOTAL TIME DEBUG: _totalExerciseTime.value is now: ${_totalExerciseTime.value}")
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel() // 🚀 NEW: Clean up rest timer job
    }

    fun completeSet(index: Int) {
        //TODO: Implement logic to mark a set as completed
        println("🔧 COMPLETE SET: Marking set ${index + 1} as completed (not implemented yet)")
        val updatedTimers = _setTimers.value.toMutableList()
        if (index in updatedTimers.indices) {
            updatedTimers[index] = updatedTimers[index].copy(isCompleted = true)
            _setTimers.value = updatedTimers
            _completedSets.value = _setTimers.value.count { it.isCompleted }
            _isExerciseCompleted.value = _completedSets.value == workoutEntry.sets
            println("🔧 COMPLETE SET: Set ${index + 1} marked as completed")
        } else {
            println("🔧 COMPLETE SET: Invalid set index $index, cannot mark as completed")
        }
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
