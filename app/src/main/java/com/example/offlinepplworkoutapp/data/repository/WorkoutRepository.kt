package com.example.offlinepplworkoutapp.data.repository

import com.example.offlinepplworkoutapp.data.dao.WorkoutDayDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutEntryDao
import com.example.offlinepplworkoutapp.data.dao.SetEntryDao
import com.example.offlinepplworkoutapp.data.dao.WorkoutTemplateDao
import com.example.offlinepplworkoutapp.data.dao.TemplateExerciseDao
import com.example.offlinepplworkoutapp.data.entity.WorkoutDay
import com.example.offlinepplworkoutapp.data.entity.WorkoutEntry
import com.example.offlinepplworkoutapp.data.entity.SetEntry
import com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate
import com.example.offlinepplworkoutapp.data.PPLTemplateData
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class WorkoutRepository(
    private val workoutDayDao: WorkoutDayDao,
    private val workoutEntryDao: WorkoutEntryDao,
    private val setEntryDao: SetEntryDao,
    private val workoutTemplateDao: WorkoutTemplateDao,
    private val templateExerciseDao: TemplateExerciseDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ===========================================
    // TEMPLATE-BASED WORKOUT CREATION (NEW)
    // ===========================================

    /**
     * Create workout from template - Main template-based workout creation method
     * This replaces the hardcoded day-based logic with flexible template system
     */
    suspend fun createWorkoutFromTemplate(templateId: Int, date: String): Flow<List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>> {
        println("🚀 REPO: Creating workout from template $templateId for date: $date")

        // Get or create workout day
        val workoutDay = getOrCreateWorkoutDayOnly(date)

        // Check if this day already has exercises
        val existingEntries = workoutEntryDao.getWorkoutEntriesForDaySync(workoutDay.id)
        if (existingEntries.isNotEmpty()) {
            println("🚀 REPO: Found existing workout day with ${existingEntries.size} exercises")
            return workoutEntryDao.getWorkoutEntriesForDay(workoutDay.id)
        }

        // Get template exercises
        val templateExercises = templateExerciseDao.getExercisesForTemplate(templateId)
        println("🚀 REPO: Got ${templateExercises.size} exercises for template $templateId")

        if (templateExercises.isNotEmpty()) {
            // Create workout entries from template
            val entries = templateExercises.map { templateExercise ->
                WorkoutEntry(
                    dayId = workoutDay.id,
                    exerciseId = templateExercise.exerciseId,
                    sets = templateExercise.sets,
                    reps = templateExercise.reps
                )
            }

            workoutEntryDao.insertAll(entries)
            println("🚀 REPO: Inserted ${entries.size} workout entries from template")

            // Create sets for each workout entry
            val insertedEntries = workoutEntryDao.getWorkoutEntriesForDaySync(workoutDay.id)
            createSetsForEntries(insertedEntries)

            // Update template last used date
            workoutTemplateDao.updateLastUsedDate(templateId, date)
        }

        return workoutEntryDao.getWorkoutEntriesForDay(workoutDay.id)
    }

    /**
     * Create today's workout using template-based system
     * Automatically determines which template to use based on day of week
     */
    suspend fun createTodaysWorkoutFromTemplate(): Flow<List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>> {
        val today = dateFormat.format(Date())
        val templateId = getTemplateIdForDate(today)

        return if (templateId > 0) {
            createWorkoutFromTemplate(templateId, today)
        } else {
            // Rest day - return empty workout
            println("🚀 REPO: Rest day - no template needed")
            workoutEntryDao.getWorkoutEntriesForDay(0) // Returns empty flow
        }
    }

    /**
     * Get template ID for a given date based on day of week
     * This maintains compatibility with current PPL schedule
     */
    private fun getTemplateIdForDate(date: String): Int {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(date) ?: Date()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return PPLTemplateData.getTemplateIdForDayOfWeek(dayOfWeek)
    }

    /**
     * Get available templates for user selection
     */
    fun getAvailableTemplates(): Flow<List<WorkoutTemplate>> {
        return workoutTemplateDao.getAllActiveTemplates()
    }

    /**
     * Get templates by category (Push/Pull/Legs)
     */
    fun getTemplatesByCategory(category: String): Flow<List<WorkoutTemplate>> {
        return workoutTemplateDao.getTemplatesByCategory(category)
    }

    // ===========================================
    // LEGACY DAY-BASED METHODS (PRESERVED)
    // ===========================================

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

        // Debug logging to see what's happening
        println("DEBUG: Creating workout for date: $date, type: $workoutType, exercises count: ${exercises.size}")

        // Only create entries if we have exercises for this workout type
        if (exercises.isNotEmpty()) {
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
            println("DEBUG: Inserted ${entries.size} workout entries for day $date")

            // 🔧 FIX: Create sets for each workout entry immediately
            // Since we just inserted the entries, we can use the entries we created
            // and get their IDs after insertion
            val insertedEntries = workoutEntryDao.getWorkoutEntriesForDaySync(dayId)
            println("🔧 REPO: Retrieved ${insertedEntries.size} inserted entries to create sets")

            for (entry in insertedEntries) {
                println("🔧 REPO: Creating sets for WorkoutEntry ID=${entry.id}, Exercise='${entry.exerciseName}', Sets=${entry.sets}")
                // Create individual sets for each exercise
                createSetsForWorkoutEntry(entry.id, entry.sets)
                println("🔧 REPO: Created ${entry.sets} sets for WorkoutEntry ID=${entry.id}")

                // Verify sets were created
                val createdSets = setEntryDao.getSetsForWorkoutEntrySync(entry.id)
                println("🔧 REPO: Verification - Found ${createdSets.size} sets for WorkoutEntry ID=${entry.id}")
                createdSets.forEach { set ->
                    println("🔧 REPO: Set ID=${set.id}, SetNumber=${set.setNumber}, WorkoutEntryId=${set.workoutEntryId}")
                }
            }

            println("🔧 REPO: All sets created for ${insertedEntries.size} exercises")
        } else {
            println("DEBUG: No exercises found for workout type: $workoutType on date: $date")
        }

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

    suspend fun toggleExerciseCompletion(entryId: Int) {
        val entry = workoutEntryDao.getWorkoutEntryById(entryId)
        entry?.let {
            val updatedEntry = it.copy(isCompleted = !it.isCompleted)
            workoutEntryDao.update(updatedEntry)
        }
    }

    suspend fun markExerciseComplete(entryId: Int, isCompleted: Boolean) {
        val entry = workoutEntryDao.getWorkoutEntryById(entryId)
        entry?.let {
            val updatedEntry = it.copy(isCompleted = isCompleted)
            workoutEntryDao.update(updatedEntry)
        }
    }

    suspend fun updateExerciseDetails(entryId: Int, sets: Int, reps: Int, isCompleted: Boolean) {
        println("🏋️ REPO: Updating exercise details - entryId: $entryId, sets: $sets, reps: $reps, isCompleted: $isCompleted")
        val entry = workoutEntryDao.getWorkoutEntryById(entryId)
        entry?.let {
            println("🏋️ REPO: Found entry - id: ${it.id}, exerciseId: ${it.exerciseId}, current isCompleted: ${it.isCompleted}")
            val updatedEntry = it.copy(sets = sets, reps = reps, isCompleted = isCompleted)
            workoutEntryDao.update(updatedEntry)
            println("🏋️ REPO: Updated entry - id: ${updatedEntry.id}, new isCompleted: ${updatedEntry.isCompleted}")
        } ?: run {
            println("🏋️ REPO ERROR: No entry found for entryId: $entryId")
        }
    }

    suspend fun updateExerciseTime(entryId: Int, totalSecondsSpent: Int) {
        val entry = workoutEntryDao.getWorkoutEntryById(entryId)
        entry?.let {
            val updatedEntry = it.copy(totalSecondsSpent = totalSecondsSpent)
            workoutEntryDao.update(updatedEntry)
        }
    }

    suspend fun startExerciseTimer(entryId: Int): Boolean {
        // Mark exercise as started but not completed
        val entry = workoutEntryDao.getWorkoutEntryById(entryId)
        return entry != null
    }

    // New methods for set-based operations
    suspend fun getSetsForWorkoutEntry(workoutEntryId: Int): Flow<List<com.example.offlinepplworkoutapp.data.entity.SetEntry>> {
        return setEntryDao.getSetsForWorkoutEntry(workoutEntryId)
    }

    suspend fun getSetsForWorkoutEntrySync(workoutEntryId: Int): List<com.example.offlinepplworkoutapp.data.entity.SetEntry> {
        return setEntryDao.getSetsForWorkoutEntrySync(workoutEntryId)
    }

    suspend fun getCompletedSetsCount(workoutEntryId: Int): Int {
        return setEntryDao.getCompletedSetsCount(workoutEntryId)
    }

    suspend fun updateSetProgress(setId: Int, isCompleted: Boolean, elapsedTimeSeconds: Int) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        setEntryDao.updateSetProgress(setId, isCompleted, elapsedTimeSeconds, completedAt)
    }

    // 🚀 NEW: Phase 2.1.1 - Methods for handling set performance data
    suspend fun updateSetProgressWithPerformanceData(
        setId: Int,
        isCompleted: Boolean,
        elapsedTimeSeconds: Int,
        repsPerformed: Int?,
        weightUsed: Float?
    ) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        setEntryDao.updateSetProgressWithPerformanceData(
            setId = setId,
            isCompleted = isCompleted,
            elapsedTimeSeconds = elapsedTimeSeconds,
            completedAt = completedAt,
            repsPerformed = repsPerformed,
            weightUsed = weightUsed
        )
        println("🏋️ REPO: Updated set $setId with performance data - reps: $repsPerformed, weight: $weightUsed")
    }

    suspend fun updateSetPerformanceDataOnly(setId: Int, repsPerformed: Int?, weightUsed: Float?) {
        setEntryDao.updateSetPerformanceData(setId, repsPerformed, weightUsed)
        println("🏋️ REPO: Updated performance data for set $setId - reps: $repsPerformed, weight: $weightUsed")
    }

    suspend fun getSetById(setId: Int): SetEntry? {
        return setEntryDao.getSetById(setId)
    }

    suspend fun createSetsForWorkoutEntry(workoutEntryId: Int, totalSets: Int) {
        val sets = (1..totalSets).map { setNumber ->
            SetEntry(
                workoutEntryId = workoutEntryId,
                setNumber = setNumber
            )
        }
        setEntryDao.insertAll(sets)
    }

    // Update exercise completion based on set completion
    suspend fun updateExerciseCompletionFromSets(workoutEntryId: Int) {
        val completedSets = setEntryDao.getCompletedSetsCount(workoutEntryId)
        val totalSets = setEntryDao.getTotalSetsCount(workoutEntryId)

        if (completedSets == totalSets && totalSets > 0) {
            // Mark exercise as completed
            val workoutEntry = workoutEntryDao.getWorkoutEntryById(workoutEntryId)
            workoutEntry?.let {
                workoutEntryDao.update(it.copy(isCompleted = true))
            }
        }
    }

    // New method that doesn't auto-create workout data
    suspend fun getTodaysWorkoutWithoutCreating(): Flow<List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>> {
        val today = dateFormat.format(Date())
        val workoutDay = workoutDayDao.getWorkoutDayByDate(today)
        return if (workoutDay != null) {
            workoutEntryDao.getWorkoutEntriesForDay(workoutDay.id)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList()) // Return empty list if no workout day exists
        }
    }

    suspend fun getWorkoutForDateWithoutCreating(date: String): Flow<List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>> {
        val workoutDay = workoutDayDao.getWorkoutDayByDate(date)
        return if (workoutDay != null) {
            workoutEntryDao.getWorkoutEntriesForDay(workoutDay.id)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList()) // Return empty list if no workout day exists
        }
    }

    // Method to manually create today's workout (called when user wants to start workout)
    suspend fun createTodaysWorkout(): Flow<List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>> {
        val today = dateFormat.format(Date())
        println("🚀 REPO: Creating today's workout for date: $today")

        // First, ensure exercises exist in the database
        val exerciseCount = workoutEntryDao.getWorkoutEntryCount() // This will check if ANY entries exist
        println("🚀 REPO: Checking if exercises are populated in database...")

        // Check if we have the basic exercise data
        val hasExercises = try {
            val exerciseDao = (workoutEntryDao as? Any) // We need to get exerciseDao reference
            // For now, let's try to insert the workout and catch the foreign key error
            true
        } catch (e: Exception) {
            false
        }

        // Check if workout day already exists
        val existingWorkoutDay = workoutDayDao.getWorkoutDayByDate(today)

        if (existingWorkoutDay != null) {
            println("🚀 REPO: Found existing workout day with ID: ${existingWorkoutDay.id}")

            // Check if it has exercises using count method
            val existingEntriesCount = workoutEntryDao.getWorkoutEntryCountForDay(existingWorkoutDay.id)
            println("🚀 REPO: Existing day has $existingEntriesCount exercises")

            if (existingEntriesCount == 0) {
                println("🚀 REPO: No exercises found, creating them now...")
                // Day exists but has no exercises, create them
                val workoutType = getWorkoutTypeForDate(today)
                val exercises = getExercisesForWorkoutType(workoutType)
                println("🚀 REPO: Got ${exercises.size} exercises for workout type: $workoutType")

                if (exercises.isNotEmpty()) {
                    try {
                        val entries = exercises.map { (exerciseId, sets, reps) ->
                            WorkoutEntry(
                                dayId = existingWorkoutDay.id,
                                exerciseId = exerciseId,
                                sets = sets,
                                reps = reps
                            )
                        }
                        workoutEntryDao.insertAll(entries)
                        println("🚀 REPO: Inserted ${entries.size} workout entries")

                        // 🔧 FIX: Create sets for each workout entry immediately
                        val insertedEntries = workoutEntryDao.getWorkoutEntriesForDaySync(existingWorkoutDay.id)
                        println("🔧 REPO: Retrieved ${insertedEntries.size} inserted entries to create sets")

                        for (entry in insertedEntries) {
                            println("🔧 REPO: Creating sets for WorkoutEntry ID=${entry.id}, Exercise='${entry.exerciseName}', Sets=${entry.sets}")
                            // Create individual sets for each exercise
                            createSetsForWorkoutEntry(entry.id, entry.sets)
                            println("🔧 REPO: Created ${entry.sets} sets for WorkoutEntry ID=${entry.id}")

                            // Verify sets were created
                            val createdSets = setEntryDao.getSetsForWorkoutEntrySync(entry.id)
                            println("🔧 REPO: Verification - Found ${createdSets.size} sets for WorkoutEntry ID=${entry.id}")
                            createdSets.forEach { set ->
                                println("🔧 REPO: Set ID=${set.id}, SetNumber=${set.setNumber}, WorkoutEntryId=${set.workoutEntryId}")
                            }
                        }

                        println("🔧 REPO: All sets created for ${insertedEntries.size} exercises")
                      } catch (e: Exception) {
                        println("🚀 REPO ERROR: Failed to insert workout entries - ${e.message}")
                        // If foreign key constraint fails, we need to populate exercises first
                        if (e.message?.contains("FOREIGN KEY constraint failed") == true) {
                            println("🚀 REPO: Exercise data missing, need to populate exercises first")
                            throw e // Re-throw to be caught by ViewModel
                        }
                    }
                }
            }
        } else {
            println("🚀 REPO: No existing workout day, creating new one...")
            // Force create the workout day and exercises
            val workoutDay = createWorkoutDayWithExercises(today)
            println("🚀 REPO: Created new workout day with ID: ${workoutDay.id}")
        }

        // Get the final workout day (either existing or newly created)
        val finalWorkoutDay = workoutDayDao.getWorkoutDayByDate(today)!!
        println("🚀 REPO: Final workout day ID: ${finalWorkoutDay.id}")

        // Return the flow of workout entries
        return workoutEntryDao.getWorkoutEntriesForDay(finalWorkoutDay.id)
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    /**
     * Create workout day only (without exercises) - used by template system
     */
    private suspend fun getOrCreateWorkoutDayOnly(date: String): WorkoutDay {
        return workoutDayDao.getWorkoutDayByDate(date) ?: run {
            val workoutDay = WorkoutDay(date = date)
            val dayId = workoutDayDao.insert(workoutDay).toInt()
            workoutDay.copy(id = dayId)
        }
    }

    /**
     * Create sets for multiple workout entries - used by template system
     */
    private suspend fun createSetsForEntries(entries: List<com.example.offlinepplworkoutapp.data.dao.WorkoutEntryWithExercise>) {
        for (entry in entries) {
            println("🔧 REPO: Creating sets for WorkoutEntry ID=${entry.id}, Exercise='${entry.exerciseName}', Sets=${entry.sets}")
            createSetsForWorkoutEntry(entry.id, entry.sets)
            println("🔧 REPO: Created ${entry.sets} sets for WorkoutEntry ID=${entry.id}")

            // Verify sets were created
            val createdSets = setEntryDao.getSetsForWorkoutEntrySync(entry.id)
            println("🔧 REPO: Verification - Found ${createdSets.size} sets for WorkoutEntry ID=${entry.id}")
            createdSets.forEach { set ->
                println("🔧 REPO: Set ID=${set.id}, SetNumber=${set.setNumber}, WorkoutEntryId=${set.workoutEntryId}")
            }
        }
        println("🔧 REPO: All sets created for ${entries.size} exercises")
    }
}

enum class WorkoutType {
    PUSH_1, PULL_1, LEGS_1, PUSH_2, PULL_2, LEGS_2, REST
}
