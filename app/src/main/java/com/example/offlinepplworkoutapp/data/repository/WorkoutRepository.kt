package com.example.offlinepplworkoutapp.data.repository

import com.example.offlinepplworkoutapp.data.dao.WorkoutDayDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryDao
import com.example.offlinepplworkoutapp.data.entity.WorkoutDay
import com.example.offlinepplworkoutapp.data.entity.WorkoutEntry
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class WorkoutRepository(
    private val workoutDayDao: WorkoutDayDao,
    private val workoutEntryDao: WorkoutEntryDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getTodaysWorkout(): Flow<List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>> {
        val today = dateFormat.format(Date())
        val workoutDay = getOrCreateWorkoutDay(today)
        return workoutEntryDao.getWorkoutEntriesForDay(workoutDay.id)
    }

    suspend fun getWorkoutForDate(date: String): Flow<List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>> {
        val workoutDay = getOrCreateWorkoutDay(date)
        return workoutEntryDao.getWorkoutEntriesForDay(workoutDay.id)
    }

    private suspend fun getOrCreateWorkoutDay(date: String): WorkoutDay {
        return workoutDayDao.getWorkoutDayByDate(date)
            ?: createWorkoutDayWithExercises(date)
    }

    private suspend fun createWorkoutDayWithExercises(date: String): WorkoutDay {
        // Create workout day
        val workoutDay = WorkoutDay(date = date)
        val dayId = workoutDayDao.insert(workoutDay).toInt()
        val createdDay = workoutDay.copy(id = dayId)

        // Determine workout type based on date
        val workoutType = getWorkoutTypeForDate(date)
        val exercises = getExercisesForWorkoutType(workoutType)

        // Create workout entries
        val entries = exercises.map { (exerciseId, sets, reps) ->
            WorkoutEntry(
                dayId = dayId,
                exerciseId = exerciseId,
                sets = sets,
                reps = reps
            )
        }

        workoutEntryDao.insertAll(entries)
        return createdDay
    }

    private fun getWorkoutTypeForDate(date: String): WorkoutType {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(date) ?: Date()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Saturday = 7

        return when (dayOfWeek) {
            Calendar.MONDAY -> WorkoutType.PUSH_1
            Calendar.TUESDAY -> WorkoutType.PULL_1
            Calendar.WEDNESDAY -> WorkoutType.LEGS_1
            Calendar.THURSDAY -> WorkoutType.PUSH_2
            Calendar.FRIDAY -> WorkoutType.PULL_2
            Calendar.SATURDAY -> WorkoutType.LEGS_2
            Calendar.SUNDAY -> WorkoutType.REST
            else -> WorkoutType.REST
        }
    }

    private fun getExercisesForWorkoutType(workoutType: WorkoutType): List<Triple<Int, Int, Int>> {
        return when (workoutType) {
            WorkoutType.PUSH_1 -> listOf(
                Triple(1, 4, 8),   // Barbell Bench Press - 4x6-8 reps
                Triple(2, 3, 10),  // Standing Overhead Press - 3x8-10 reps
                Triple(3, 3, 12),  // Incline Dumbbell Press - 3x8-12 reps
                Triple(4, 3, 15),  // Dumbbell Lateral Raise - 3x12-15 reps
                Triple(5, 3, 12)   // Cable Triceps Pushdown - 3x10-12 reps
            )
            WorkoutType.PULL_1 -> listOf(
                Triple(6, 3, 8),   // Deadlift - 3x5-8 reps
                Triple(7, 3, 10),  // Pull-Ups or Lat Pulldowns - 3x8-10 reps
                Triple(8, 3, 12),  // Bent-Over Barbell Row - 3x8-12 reps
                Triple(9, 3, 15),  // Face Pull - 3x12-15 reps
                Triple(10, 3, 12), // Barbell Biceps Curl - 3x8-12 reps
                Triple(11, 2, 12)  // Hammer Curl - 2x10-12 reps
            )
            WorkoutType.LEGS_1 -> listOf(
                Triple(12, 4, 8),  // Back Squat - 4x6-8 reps
                Triple(13, 3, 12), // Romanian Deadlift - 3x8-12 reps
                Triple(14, 3, 12), // Leg Press - 3x10-12 reps
                Triple(15, 3, 12), // Lying Leg Curl - 3x10-12 reps
                Triple(16, 4, 15)  // Seated Calf Raise - 4x12-15 reps
            )
            WorkoutType.PUSH_2 -> listOf(
                Triple(17, 4, 8),  // Standing Overhead Press - 4x6-8 reps
                Triple(18, 3, 12), // Incline Barbell Press - 3x8-12 reps
                Triple(19, 3, 10), // Weighted Dips - 3x8-10 reps
                Triple(20, 3, 15), // Cable Lateral Raise - 3x12-15 reps
                Triple(21, 3, 15), // Pec Deck or Dumbbell Fly - 3x12-15 reps
                Triple(22, 3, 12)  // Overhead Cable Triceps Extension - 3x10-12 reps
            )
            WorkoutType.PULL_2 -> listOf(
                Triple(23, 4, 10), // Pendlay or Bent-Over Row - 4x6-10 reps
                Triple(24, 3, 12), // Weighted Pull-Ups or Wide-Grip Lat Pulldown - 3x8-12 reps
                Triple(25, 3, 12), // Dumbbell Shrug - 3x10-12 reps
                Triple(26, 3, 15), // Face Pull - 3x12-15 reps
                Triple(27, 3, 12), // EZ-Bar Biceps Curl - 3x8-12 reps
                Triple(28, 2, 12)  // Reverse Grip or Preacher Curl - 2x10-12 reps
            )
            WorkoutType.LEGS_2 -> listOf(
                Triple(29, 4, 8),  // Front Squat - 4x6-8 reps
                Triple(30, 3, 10), // Bulgarian Split Squat - 3x8-10 reps (each leg)
                Triple(31, 3, 12), // Barbell Hip Thrust - 3x10-12 reps
                Triple(32, 3, 15), // Leg Extension - 3x12-15 reps
                Triple(33, 3, 15), // Seated or Lying Leg Curl - 3x12-15 reps
                Triple(34, 4, 15)  // Standing Calf Raise - 4x12-15 reps
            )
            WorkoutType.REST -> emptyList()
        }
    }

    suspend fun updateWorkoutEntry(entry: WorkoutEntry) {
        workoutEntryDao.update(entry)
    }
}

enum class WorkoutType {
    PUSH_1, PULL_1, LEGS_1, PUSH_2, PULL_2, LEGS_2, REST
}
