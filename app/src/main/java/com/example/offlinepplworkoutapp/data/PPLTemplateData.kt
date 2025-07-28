package com.example.offlinepplworkoutapp.data

import com.example.offlinepplworkoutapp.data.entity.WorkoutTemplate
import com.example.offlinepplworkoutapp.data.entity.TemplateExercise

/**
 * PPL Workout Templates Data
 *
 * This defines the predefined workout templates that replace the hardcoded day-based logic.
 * Each template contains a structured workout with specific exercises, sets, reps, and rest periods.
 */
object PPLTemplateData {

    fun getPPLTemplates(): List<WorkoutTemplate> {
        return listOf(
            // PUSH DAY TEMPLATES
            WorkoutTemplate(
                id = 1,
                name = "Push Day 1 - Chest Focus",
                description = "Chest-focused push workout with barbell and dumbbell movements",
                estimatedDuration = 75,
                difficulty = "Intermediate",
                category = "Push",
                isCustom = false,
                isActive = true
            ),
            WorkoutTemplate(
                id = 2,
                name = "Push Day 2 - Shoulder Focus",
                description = "Shoulder-focused push workout with overhead pressing emphasis",
                estimatedDuration = 70,
                difficulty = "Intermediate",
                category = "Push",
                isCustom = false,
                isActive = true
            ),

            // PULL DAY TEMPLATES
            WorkoutTemplate(
                id = 3,
                name = "Pull Day 1 - Back Width",
                description = "Back width focused pull workout with lat pulldowns and rows",
                estimatedDuration = 75,
                difficulty = "Intermediate",
                category = "Pull",
                isCustom = false,
                isActive = true
            ),
            WorkoutTemplate(
                id = 4,
                name = "Pull Day 2 - Back Thickness",
                description = "Back thickness focused pull workout with heavy rows and deadlifts",
                estimatedDuration = 80,
                difficulty = "Intermediate",
                category = "Pull",
                isCustom = false,
                isActive = true
            ),

            // LEGS DAY TEMPLATES
            WorkoutTemplate(
                id = 5,
                name = "Legs Day 1 - Quad Focus",
                description = "Quad-focused leg workout with squats and extensions",
                estimatedDuration = 85,
                difficulty = "Intermediate",
                category = "Legs",
                isCustom = false,
                isActive = true
            ),
            WorkoutTemplate(
                id = 6,
                name = "Legs Day 2 - Posterior Chain",
                description = "Hamstring and glute focused leg workout with deadlifts and curls",
                estimatedDuration = 80,
                difficulty = "Intermediate",
                category = "Legs",
                isCustom = false,
                isActive = true
            )
        )
    }

    fun getTemplateExercises(): List<TemplateExercise> {
        return listOf(
            // PUSH DAY 1 - CHEST FOCUS (Template ID: 1)
            TemplateExercise(templateId = 1, exerciseId = 1, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180), // Barbell Bench Press
            TemplateExercise(templateId = 1, exerciseId = 2, orderIndex = 1, sets = 4, reps = 8, restSeconds = 180), // Standing Overhead Press
            TemplateExercise(templateId = 1, exerciseId = 3, orderIndex = 2, sets = 3, reps = 10, restSeconds = 120), // Incline Dumbbell Press
            TemplateExercise(templateId = 1, exerciseId = 4, orderIndex = 3, sets = 3, reps = 12, restSeconds = 90), // Dumbbell Flyes
            TemplateExercise(templateId = 1, exerciseId = 5, orderIndex = 4, sets = 3, reps = 12, restSeconds = 90), // Tricep Dips

            // PUSH DAY 2 - SHOULDER FOCUS (Template ID: 2)
            TemplateExercise(templateId = 2, exerciseId = 2, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180), // Standing Overhead Press
            TemplateExercise(templateId = 2, exerciseId = 1, orderIndex = 1, sets = 4, reps = 8, restSeconds = 180), // Barbell Bench Press
            TemplateExercise(templateId = 2, exerciseId = 6, orderIndex = 2, sets = 3, reps = 12, restSeconds = 90), // Lateral Raises
            TemplateExercise(templateId = 2, exerciseId = 7, orderIndex = 3, sets = 3, reps = 12, restSeconds = 90), // Rear Delt Flyes
            TemplateExercise(templateId = 2, exerciseId = 8, orderIndex = 4, sets = 3, reps = 12, restSeconds = 90), // Close-Grip Bench Press

            // PULL DAY 1 - BACK WIDTH (Template ID: 3)
            TemplateExercise(templateId = 3, exerciseId = 9, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180), // Deadlifts
            TemplateExercise(templateId = 3, exerciseId = 10, orderIndex = 1, sets = 4, reps = 10, restSeconds = 120), // Pull-ups/Lat Pulldowns
            TemplateExercise(templateId = 3, exerciseId = 11, orderIndex = 2, sets = 4, reps = 10, restSeconds = 120), // Pendlay or Bent-Over Row
            TemplateExercise(templateId = 3, exerciseId = 12, orderIndex = 3, sets = 3, reps = 12, restSeconds = 90), // Cable Rows
            TemplateExercise(templateId = 3, exerciseId = 13, orderIndex = 4, sets = 3, reps = 12, restSeconds = 90), // Face Pulls
            TemplateExercise(templateId = 3, exerciseId = 14, orderIndex = 5, sets = 4, reps = 12, restSeconds = 90), // Barbell or Dumbbell Curls

            // PULL DAY 2 - BACK THICKNESS (Template ID: 4)
            TemplateExercise(templateId = 4, exerciseId = 11, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180), // Pendlay or Bent-Over Row
            TemplateExercise(templateId = 4, exerciseId = 9, orderIndex = 1, sets = 4, reps = 6, restSeconds = 180), // Deadlifts
            TemplateExercise(templateId = 4, exerciseId = 15, orderIndex = 2, sets = 3, reps = 10, restSeconds = 120), // T-Bar Row
            TemplateExercise(templateId = 4, exerciseId = 16, orderIndex = 3, sets = 3, reps = 12, restSeconds = 90), // Hammer Curls
            TemplateExercise(templateId = 4, exerciseId = 17, orderIndex = 4, sets = 3, reps = 12, restSeconds = 90), // Cable Hammer Curls
            TemplateExercise(templateId = 4, exerciseId = 18, orderIndex = 5, sets = 3, reps = 15, restSeconds = 60), // Shrugs

            // LEGS DAY 1 - QUAD FOCUS (Template ID: 5)
            TemplateExercise(templateId = 5, exerciseId = 19, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180), // Back Squat
            TemplateExercise(templateId = 5, exerciseId = 20, orderIndex = 1, sets = 4, reps = 10, restSeconds = 120), // Romanian Deadlifts
            TemplateExercise(templateId = 5, exerciseId = 21, orderIndex = 2, sets = 3, reps = 12, restSeconds = 90), // Bulgarian Split Squat
            TemplateExercise(templateId = 5, exerciseId = 22, orderIndex = 3, sets = 3, reps = 15, restSeconds = 90), // Leg Press
            TemplateExercise(templateId = 5, exerciseId = 23, orderIndex = 4, sets = 3, reps = 15, restSeconds = 90), // Leg Extension
            TemplateExercise(templateId = 5, exerciseId = 24, orderIndex = 5, sets = 4, reps = 15, restSeconds = 60), // Standing Calf Raise

            // LEGS DAY 2 - POSTERIOR CHAIN (Template ID: 6)
            TemplateExercise(templateId = 6, exerciseId = 25, orderIndex = 0, sets = 4, reps = 8, restSeconds = 180), // Front Squat
            TemplateExercise(templateId = 6, exerciseId = 26, orderIndex = 1, sets = 3, reps = 12, restSeconds = 120), // Bulgarian Split Squat
            TemplateExercise(templateId = 6, exerciseId = 27, orderIndex = 2, sets = 3, reps = 12, restSeconds = 120), // Barbell Hip Thrust
            TemplateExercise(templateId = 6, exerciseId = 28, orderIndex = 3, sets = 3, reps = 15, restSeconds = 90), // Leg Extension
            TemplateExercise(templateId = 6, exerciseId = 29, orderIndex = 4, sets = 3, reps = 15, restSeconds = 90), // Seated or Lying Leg Curl
            TemplateExercise(templateId = 6, exerciseId = 30, orderIndex = 5, sets = 4, reps = 15, restSeconds = 60) // Standing Calf Raise
        )
    }

    /**
     * Gets the appropriate template ID based on day of week and current cycle
     * This maintains backward compatibility with the existing day-based system
     */
    fun getTemplateIdForDay(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            1 -> 1 // Monday: Push Day 1 - Chest Focus
            2 -> 3 // Tuesday: Pull Day 1 - Back Width
            3 -> 5 // Wednesday: Legs Day 1 - Quad Focus
            4 -> 2 // Thursday: Push Day 2 - Shoulder Focus
            5 -> 4 // Friday: Pull Day 2 - Back Thickness
            6 -> 6 // Saturday: Legs Day 2 - Posterior Chain
            else -> 1 // Default to Push Day 1
        }
    }
}
