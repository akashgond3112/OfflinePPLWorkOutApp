package com.example.offlinepplworkoutapp.data

import com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate
import com.example.offlinepplworkoutapp.data.entity.TemplateExercise

/**
 * PPL Template Library - Predefined workout templates for Push/Pull/Legs program
 * 
 * This class provides the 6 core PPL workout templates that replace the hardcoded
 * day-based workout logic. Each template contains exercises with proper sets, reps,
 * and rest periods for optimal training progression.
 */
object PPLTemplateData {

    /**
     * Returns the 6 predefined PPL workout templates
     */
    fun getPPLTemplates(): List<WorkoutTemplate> {
        return listOf(
            // Template 1: Push Day 1 (Monday)
            WorkoutTemplate(
                id = 1,
                name = "Push Day 1",
                description = "Chest and Triceps focused with Overhead Press",
                estimatedDuration = 75,
                difficulty = "Intermediate",
                category = "Push",
                isCustom = false,
                isActive = true,
                createdDate = "2025-07-29",
                lastUsedDate = ""
            ),

            // Template 2: Pull Day 1 (Tuesday)
            WorkoutTemplate(
                id = 2,
                name = "Pull Day 1",
                description = "Back and Biceps with Deadlift focus",
                estimatedDuration = 80,
                difficulty = "Intermediate",
                category = "Pull",
                isCustom = false,
                isActive = true,
                createdDate = "2025-07-29",
                lastUsedDate = ""
            ),

            // Template 3: Legs Day 1 (Wednesday)
            WorkoutTemplate(
                id = 3,
                name = "Legs Day 1",
                description = "Quad and Glute focused with Back Squat",
                estimatedDuration = 70,
                difficulty = "Intermediate",
                category = "Legs",
                isCustom = false,
                isActive = true,
                createdDate = "2025-07-29",
                lastUsedDate = ""
            ),

            // Template 4: Push Day 2 (Thursday)
            WorkoutTemplate(
                id = 4,
                name = "Push Day 2",
                description = "Shoulder and Triceps focused with Dips",
                estimatedDuration = 75,
                difficulty = "Intermediate",
                category = "Push",
                isCustom = false,
                isActive = true,
                createdDate = "2025-07-29",
                lastUsedDate = ""
            ),

            // Template 5: Pull Day 2 (Friday)
            WorkoutTemplate(
                id = 5,
                name = "Pull Day 2",
                description = "Back width and Biceps with Row focus",
                estimatedDuration = 80,
                difficulty = "Intermediate",
                category = "Pull",
                isCustom = false,
                isActive = true,
                createdDate = "2025-07-29",
                lastUsedDate = ""
            ),

            // Template 6: Legs Day 2 (Saturday)
            WorkoutTemplate(
                id = 6,
                name = "Legs Day 2",
                description = "Hamstring and Calf focused with Front Squat",
                estimatedDuration = 70,
                difficulty = "Intermediate",
                category = "Legs",
                isCustom = false,
                isActive = true,
                createdDate = "2025-07-29",
                lastUsedDate = ""
            )
        )
    }

    /**
     * Returns all template-exercise relationships with proper configuration
     * 
     * This maps each exercise to its template with sets, reps, rest periods,
     * and order based on the current PPL program structure.
     */
    fun getTemplateExercises(): List<TemplateExercise> {
        return listOf(
            // PUSH DAY 1 (Template ID 1) - 5 exercises
            TemplateExercise(templateId = 1, exerciseId = 1, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180),   // Barbell Bench Press
            TemplateExercise(templateId = 1, exerciseId = 2, orderIndex = 1, sets = 3, reps = 10, restSeconds = 120),  // Standing Overhead Press
            TemplateExercise(templateId = 1, exerciseId = 3, orderIndex = 2, sets = 3, reps = 12, restSeconds = 90),   // Incline Dumbbell Press
            TemplateExercise(templateId = 1, exerciseId = 4, orderIndex = 3, sets = 3, reps = 15, restSeconds = 60),   // Dumbbell Lateral Raise
            TemplateExercise(templateId = 1, exerciseId = 5, orderIndex = 4, sets = 3, reps = 12, restSeconds = 60),   // Cable Triceps Pushdown

            // PULL DAY 1 (Template ID 2) - 6 exercises
            TemplateExercise(templateId = 2, exerciseId = 6, orderIndex = 0, sets = 3, reps = 8, restSeconds = 180),   // Deadlift
            TemplateExercise(templateId = 2, exerciseId = 7, orderIndex = 1, sets = 3, reps = 10, restSeconds = 120),  // Pull-Ups or Lat Pulldowns
            TemplateExercise(templateId = 2, exerciseId = 8, orderIndex = 2, sets = 3, reps = 12, restSeconds = 90),   // Bent-Over Barbell Row
            TemplateExercise(templateId = 2, exerciseId = 9, orderIndex = 3, sets = 3, reps = 15, restSeconds = 60),   // Face Pull
            TemplateExercise(templateId = 2, exerciseId = 10, orderIndex = 4, sets = 3, reps = 12, restSeconds = 60),  // Barbell Biceps Curl
            TemplateExercise(templateId = 2, exerciseId = 11, orderIndex = 5, sets = 2, reps = 12, restSeconds = 60),  // Hammer Curl

            // LEGS DAY 1 (Template ID 3) - 5 exercises
            TemplateExercise(templateId = 3, exerciseId = 12, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180),  // Back Squat
            TemplateExercise(templateId = 3, exerciseId = 13, orderIndex = 1, sets = 3, reps = 12, restSeconds = 120), // Romanian Deadlift
            TemplateExercise(templateId = 3, exerciseId = 14, orderIndex = 2, sets = 3, reps = 12, restSeconds = 90),  // Leg Press
            TemplateExercise(templateId = 3, exerciseId = 15, orderIndex = 3, sets = 3, reps = 12, restSeconds = 60),  // Lying Leg Curl
            TemplateExercise(templateId = 3, exerciseId = 16, orderIndex = 4, sets = 4, reps = 15, restSeconds = 45),  // Seated Calf Raise

            // PUSH DAY 2 (Template ID 4) - 6 exercises
            TemplateExercise(templateId = 4, exerciseId = 17, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180),  // Standing Overhead Press
            TemplateExercise(templateId = 4, exerciseId = 18, orderIndex = 1, sets = 3, reps = 12, restSeconds = 120), // Incline Barbell Press
            TemplateExercise(templateId = 4, exerciseId = 19, orderIndex = 2, sets = 3, reps = 10, restSeconds = 90),  // Weighted Dips
            TemplateExercise(templateId = 4, exerciseId = 20, orderIndex = 3, sets = 3, reps = 15, restSeconds = 60),  // Cable Lateral Raise
            TemplateExercise(templateId = 4, exerciseId = 21, orderIndex = 4, sets = 3, reps = 15, restSeconds = 60),  // Pec Deck or Dumbbell Fly
            TemplateExercise(templateId = 4, exerciseId = 22, orderIndex = 5, sets = 3, reps = 12, restSeconds = 60),  // Overhead Cable Triceps Extension

            // PULL DAY 2 (Template ID 5) - 6 exercises
            TemplateExercise(templateId = 5, exerciseId = 23, orderIndex = 0, sets = 4, reps = 10, restSeconds = 180), // Pendlay or Bent-Over Row
            TemplateExercise(templateId = 5, exerciseId = 24, orderIndex = 1, sets = 3, reps = 12, restSeconds = 120), // Weighted Pull-Ups or Wide-Grip Lat Pulldown
            TemplateExercise(templateId = 5, exerciseId = 25, orderIndex = 2, sets = 3, reps = 12, restSeconds = 90),  // Dumbbell Shrug
            TemplateExercise(templateId = 5, exerciseId = 26, orderIndex = 3, sets = 3, reps = 15, restSeconds = 60),  // Face Pull
            TemplateExercise(templateId = 5, exerciseId = 27, orderIndex = 4, sets = 3, reps = 12, restSeconds = 60),  // EZ-Bar Biceps Curl
            TemplateExercise(templateId = 5, exerciseId = 28, orderIndex = 5, sets = 2, reps = 12, restSeconds = 60),  // Reverse Grip or Preacher Curl

            // LEGS DAY 2 (Template ID 6) - 6 exercises
            TemplateExercise(templateId = 6, exerciseId = 29, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180),  // Front Squat
            TemplateExercise(templateId = 6, exerciseId = 30, orderIndex = 1, sets = 3, reps = 10, restSeconds = 120), // Bulgarian Split Squat
            TemplateExercise(templateId = 6, exerciseId = 31, orderIndex = 2, sets = 3, reps = 12, restSeconds = 90),  // Barbell Hip Thrust
            TemplateExercise(templateId = 6, exerciseId = 32, orderIndex = 3, sets = 3, reps = 15, restSeconds = 60),  // Leg Extension
            TemplateExercise(templateId = 6, exerciseId = 33, orderIndex = 4, sets = 3, reps = 15, restSeconds = 60),  // Seated or Lying Leg Curl
            TemplateExercise(templateId = 6, exerciseId = 34, orderIndex = 5, sets = 4, reps = 15, restSeconds = 45)   // Standing Calf Raise
        )
    }

    /**
     * Helper function to get template ID for a given day of week
     * This maintains compatibility with current day-based logic
     */
    fun getTemplateIdForDayOfWeek(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            2 -> 1 // Monday -> Push Day 1
            3 -> 2 // Tuesday -> Pull Day 1
            4 -> 3 // Wednesday -> Legs Day 1
            5 -> 4 // Thursday -> Push Day 2
            6 -> 5 // Friday -> Pull Day 2
            7 -> 6 // Saturday -> Legs Day 2
            1 -> 0 // Sunday -> Rest Day (no template)
            else -> 0 // Rest Day
        }
    }

    /**
     * Helper function to get exercises for a specific template
     * Returns list of exercise configurations for the given template
     */
    fun getExercisesForTemplate(templateId: Int): List<TemplateExercise> {
        return getTemplateExercises().filter { it.templateId == templateId }
    }
}
