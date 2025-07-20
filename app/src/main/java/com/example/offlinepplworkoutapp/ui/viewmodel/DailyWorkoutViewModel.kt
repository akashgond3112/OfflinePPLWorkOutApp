package com.example.offlinepplworkoutapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
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

    private val _todaysWorkout = MutableStateFlow<List<WorkoutEntryWithExercise>>(emptyList())
    val todaysWorkout: StateFlow<List<WorkoutEntryWithExercise>> = _todaysWorkout.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        loadTodaysWorkout()
    }

    private fun loadTodaysWorkout() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTodaysWorkout().collect { exercises ->
                _todaysWorkout.value = exercises
                _isLoading.value = false
            }
        }
    }

    fun loadWorkoutForDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentDate.value = date
            repository.getWorkoutForDate(date).collect { exercises ->
                _todaysWorkout.value = exercises
                _isLoading.value = false
            }
        }
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
