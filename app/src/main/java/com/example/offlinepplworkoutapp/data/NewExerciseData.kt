package com.example.offlinepplworkoutapp.data

import com.example.offlinepplworkoutapp.data.entity.Exercise

/**
 * New exercises to add to the database without affecting existing data
 * Used for database migration from version 10 to 11
 */
object NewExerciseData {

    /**
     * Returns only the new exercises that should be added to existing databases
     */
    fun getNewExercises(): List<Exercise> {
        // Return only the new exercises you want to add to the database
        // Make sure the IDs don't conflict with existing exercises
        return listOf(
            Exercise(
                id = 35,
                name = "Close-Grip Bench Press",
                isCompound = true,
                primaryMuscle = "Triceps",
                secondaryMuscles = "Chest,Front Delts",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Lie flat on bench with hands shoulder-width apart\n2. Lower bar to lower chest/mid-sternum\n3. Press bar up while keeping elbows close to body\n4. Focus on triceps contraction at lockout",
                tips = "Elbows should stay tucked, not flared. Reduce weight compared to regular bench press. Full lockout emphasizes triceps.",
                category = "Push"
            ),
            Exercise(
                id = 36,
                name = "Dumbbell Overhead Press",
                isCompound = true,
                primaryMuscle = "Shoulders",
                secondaryMuscles = "Triceps,Upper Chest",
                equipment = "Dumbbells",
                difficulty = "Beginner",
                instructions = "1. Sit on bench with back support\n2. Hold dumbbells at shoulder height\n3. Press straight overhead until arms fully extended\n4. Lower with control to ear level",
                tips = "Keep palms facing forward. Don't lock elbows aggressively. Seated position reduces back arching.",
                category = "Push"
            ),
            Exercise(
                id = 37,
                name = "Cable Chest Press",
                isCompound = false,
                primaryMuscle = "Chest",
                secondaryMuscles = "Shoulders,Triceps",
                equipment = "Cable Machine",
                difficulty = "Beginner",
                instructions = "1. Set cables to chest height\n2. Step forward into staggered stance\n3. Press handles forward and together\n4. Slowly return to starting position",
                tips = "Maintain constant tension on chest. Squeeze pecs at midline. Don't let shoulders round forward.",
                category = "Push"
            ),

            Exercise(
                id = 38,
                name = "Single-Arm Dumbbell Row",
                isCompound = true,
                primaryMuscle = "Back",
                secondaryMuscles = "Biceps,Traps",
                equipment = "Dumbbell",
                difficulty = "Beginner",
                instructions = "1. Place knee and hand on bench\n2. Let dumbbell hang straight down\n3. Pull elbow up past torso\n4. Lower with full stretch\n5. Repeat all reps on one side before switching",
                tips = "Keep torso parallel to floor. Focus on scapular retraction. Don't rotate torso during pull.",
                category = "Pull"
            ),
            Exercise(
                id = 39,
                name = "Chin-Ups",
                isCompound = true,
                primaryMuscle = "Biceps",
                secondaryMuscles = "Back",
                equipment = "Pull-up Bar",
                difficulty = "Intermediate",
                instructions = "1. Grip bar with palms facing you\n2. Hang with arms fully extended\n3. Pull up until chin clears bar\n4. Lower with control",
                tips = "Tuck elbows slightly toward ribs. Squeeze biceps at top. Lean back slightly to engage lats.",
                category = "Pull"
            ),
            Exercise(
                id = 40,
                name = "Seated Cable Row",
                isCompound = true,
                primaryMuscle = "Back",
                secondaryMuscles = "Biceps,Rear Delts",
                equipment = "Cable Machine",
                difficulty = "Beginner",
                instructions = "1. Sit with knees slightly bent\n2. Grip handle with neutral grip\n3. Pull handle to lower abdomen\n4. Squeeze shoulder blades together\n5. Return with controlled stretch",
                tips = "Maintain upright torso. Don't use momentum. Pause at contraction point.",
                category = "Pull"
            ),

            Exercise(
                id = 41,
                name = "Goblet Squat",
                isCompound = true,
                primaryMuscle = "Quadriceps",
                secondaryMuscles = "Glutes,Core",
                equipment = "Dumbbell/Kettlebell",
                difficulty = "Beginner",
                instructions = "1. Hold weight vertically against chest\n2. Stand with feet wider than shoulders\n3. Descend deep between thighs\n4. Drive through heels to stand",
                tips = "Keep elbows inside knees. Maintain upright torso. Excellent for depth practice.",
                category = "Legs"
            ),
            Exercise(
                id = 42,
                name = "Glute Ham Raise",
                isCompound = true,
                primaryMuscle = "Hamstrings",
                secondaryMuscles = "Glutes,Calves",
                equipment = "GHR Machine",
                difficulty = "Advanced",
                instructions = "1. Secure ankles in machine\n2. Start from kneeling position\n3. Lower body slowly while keeping hips extended\n4. Pull back up using hamstrings",
                tips = "Maintain straight line from knees to shoulders. Use arms for assistance if needed. Control eccentric phase.",
                category = "Legs"
            ),
            Exercise(
                id = 43,
                name = "Walking Lunges",
                isCompound = true,
                primaryMuscle = "Quadriceps",
                secondaryMuscles = "Glutes,Hamstrings,Calves",
                equipment = "Dumbbells/Bodyweight",
                difficulty = "Intermediate",
                instructions = "1. Stand with feet together holding weights\n2. Step forward into lunge position\n3. Lower back knee toward floor\n4. Drive through front heel to step into next lunge",
                tips = "Keep torso upright. Don't let front knee pass toes. Maintain fluid walking rhythm.",
                category = "Legs"
            ),
            Exercise(
                id = 44,
                name = "Treadmill Running",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Quadriceps,Calves,Glutes",
                equipment = "Treadmill",
                difficulty = "Beginner",
                instructions = "1. Start walking at 2-3 mph to warm up\n2. Gradually increase speed to running pace (5-8 mph)\n3. Maintain upright posture with slight forward lean\n4. Land mid-foot with knees slightly bent\n5. Cool down with 5-minute walk",
                tips = "Use 1-3% incline to simulate outdoor conditions. Interval training: Alternate 1 min sprint/2 min recovery",
                category = "Cardio"
            ),
            Exercise(
                id = 45,
                name = "Stair Climber",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Glutes,Quadriceps,Calves",
                equipment = "Stair Machine",
                difficulty = "Intermediate",
                instructions = "1. Stand tall with slight forward lean\n2. Place entire foot on each step\n3. Drive through heels to activate glutes\n4. Maintain rhythmic breathing pattern\n5. Increase resistance gradually",
                tips = "Avoid leaning on handrails - reduces calorie burn by 30% :cite[6]. For HIIT: 30s fast pace/60s recovery",
                category = "Cardio"
            ),
            Exercise(
                id = 46,
                name = "Rowing Machine Sprints",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Back,Shoulders,Legs",
                equipment = "Rowing Machine",
                difficulty = "Intermediate",
                instructions = "1. Start with knees bent, arms extended\n2. Drive through legs while leaning back slightly\n3. Pull handle to lower ribs\n4. Return to start position in reverse order\n5. Maintain 20-30 strokes/minute pace",
                tips = "60% power from legs, 20% core, 20% arms. HIIT protocol: 500m sprint/1 min rest x 8 rounds :cite[5]",
                category = "Cardio"
            ),
            Exercise(
                id = 47,
                name = "Cycling Sprints",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Quadriceps,Glutes,Calves",
                equipment = "Stationary Bike",
                difficulty = "Beginner",
                instructions = "1. Adjust seat height to hip level\n2. Start pedaling at light resistance\n3. Increase resistance to challenging level\n4. Maintain 80-100 RPM cadence\n5. Alternate seated/standing positions",
                tips = "Norwegian 4x4 protocol: 4 min max effort/3 min recovery x 4 cycles :cite[5]. Keeps joints protected while elevating heart rate :cite[2]",
                category = "Cardio"
            ),
            Exercise(
                id = 48,
                name = "Elliptical Intervals",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Quadriceps,Glutes,Hamstrings",
                equipment = "Elliptical Machine",
                difficulty = "Beginner",
                instructions = "1. Stand tall with hands on moving handles\n2. Start with low resistance warm-up\n3. Increase resistance and stride length\n4. Push through heels to engage posterior chain\n5. Incorporate backward motion every 5 minutes",
                tips = "For fat loss: 2 min high resistance/1 min recovery. Zero-impact on joints makes ideal for active recovery :cite[2]",
                category = "Cardio"
            ),
            Exercise(
                id = 49,
                name = "Swimming Intervals",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Back,Shoulders,Core",
                equipment = "None",
                difficulty = "Intermediate",
                instructions = "1. Choose stroke (freestyle recommended)\n2. Swim 50m at 80% max effort\n3. Rest 20-30 seconds\n4. Repeat 8-12 times\n5. Use kickboard for leg-focused intervals",
                tips = "Buoyancy reduces joint impact by 90% vs land exercises :cite[2]. Alternate strokes to engage different muscles",
                category = "Cardio"
            ),
            Exercise(
                id = 50,
                name = "Running Intervals",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Quadriceps,Calves,Glutes",
                equipment = "None",
                difficulty = "Intermediate",
                instructions = "1. Warm up with 5 min jog\n2. Sprint 30 seconds at 90% max effort\n3. Walk 60 seconds for recovery\n4. Repeat 8-12 cycles\n5. Cool down with light jogging",
                tips = "Improves VO2 max more effectively than steady-state cardio :cite[8]. Use hill sprints for glute activation",
                category = "Cardio"
            ),
            Exercise(
                id = 51,
                name = "Jump Rope HIIT",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Calves,Shoulders,Core",
                equipment = "Jump Rope",
                difficulty = "Intermediate",
                instructions = "1. Adjust rope length to armpit height when standing on center\n2. Keep elbows close, rotate with wrists\n3. Jump 1-2 inches high with soft landings\n4. Alternate basic jumps with high knees\n5. Interval format: 45s work/15s rest",
                tips = "Burns 15-20 calories/minute. Advanced: Double unders (rope passes twice per jump) :cite[6]",
                category = "Cardio"
            ),
            Exercise(
                id = 52,
                name = "Mountain Climbers",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Core,Shoulders,Chest",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Start in high plank position\n2. Bring right knee toward chest\n3. Quickly switch legs in running motion\n4. Maintain flat back and engaged core\n5. Keep hips level throughout",
                tips = "For HIIT: 20s on/10s off x 8 rounds. Modifications: Slow tempo or step-back mountain climbers :cite[4]",
                category = "Cardio"
            ),
            Exercise(
                id = 53,
                name = "Burpees",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Chest,Shoulders,Legs,Core",
                equipment = "None",
                difficulty = "Advanced",
                instructions = "1. Stand with feet shoulder-width\n2. Squat down and place hands on floor\n3. Kick feet back to plank position\n4. Perform push-up (optional)\n5. Jump feet back to hands and explode upward",
                tips = "Full-body calorie torch: 10 reps burn ~100 calories. Beginner version: Step back instead of jump :cite[8]",
                category = "Cardio"
            ),
            Exercise(
                id = 54,
                name = "High Knees",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Hip Flexors,Core,Calves",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Stand tall with feet hip-width\n2. Drive knees upward to waist level\n3. Pump arms opposite to legs\n4. Land on balls of feet\n5. Maintain quick, rhythmic pace",
                tips = "Keeps heart rate in fat-burning zone (60-70% max HR). For endurance: 60s on/30s off x 10 rounds :cite[4]",
                category = "Cardio"
            ),
            Exercise(
                id = 55,
                name = "Walking Lunges",
                isCompound = true,
                primaryMuscle = "Cardiovascular",
                secondaryMuscles = "Quadriceps,Glutes,Hamstrings",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Stand with hands on hips or holding weights\n2. Step forward into lunge, both knees at 90°\n3. Drive through front heel to stand\n4. Immediately step forward with opposite leg\n5. Maintain continuous walking motion",
                tips = "Add cardio challenge: Lunge jumps. For joint protection: Shorter strides :cite[4]",
                category = "Cardio"
            ),
            Exercise(
                id = 56,
                name = "Hanging Leg Raise",
                isCompound = true,
                primaryMuscle = "Abs",
                secondaryMuscles = "Hip Flexors,Forearms",
                equipment = "Pull-up Bar",
                difficulty = "Intermediate",
                instructions = "1. Hang from bar with overhand grip\n2. Engage lats to prevent swinging\n3. Raise straight legs to 90°\n4. Slowly lower with control\n5. Prevent momentum at bottom",
                tips = "Advanced: Add ankle weights. For obliques: Raise legs to sides :cite[7]",
                category = "Abs"
            ),
            Exercise(
                id = 57,
                name = "Ab Wheel Rollout",
                isCompound = true,
                primaryMuscle = "Abs",
                secondaryMuscles = "Shoulders,Latissimus Dorsi",
                equipment = "Ab Wheel",
                difficulty = "Advanced",
                instructions = "1. Kneel holding ab wheel handles\n2. Brace core and roll forward slowly\n3. Extend until body nearly parallel to floor\n4. Contract abs to pull back to start\n5. Maintain straight body line",
                tips = "Beginner: Roll against wall or reduce range. Common mistake: Sagging hips :cite[7]",
                category = "Abs"
            ),
            Exercise(
                id = 58,
                name = "Medicine Ball Slam",
                isCompound = true,
                primaryMuscle = "Abs",
                secondaryMuscles = "Shoulders,Back,Glutes",
                equipment = "Medicine Ball",
                difficulty = "Intermediate",
                instructions = "1. Stand with feet wider than shoulders\n2. Raise ball overhead with straight arms\n3. Slam ball down forcefully using core\n4. Catch rebound and immediately repeat\n5. Engage entire core during extension",
                tips = "Power originates from core rotation. Use 6-15 lb ball for optimal speed-power balance :cite[7]",
                category = "Abs"
            ),
            Exercise(
                id = 59,
                name = "Renegade Rows",
                isCompound = true,
                primaryMuscle = "Abs",
                secondaryMuscles = "Back,Biceps,Shoulders",
                equipment = "Dumbbells",
                difficulty = "Advanced",
                instructions = "1. Start in plank position holding dumbbells\n2. Row right dumbbell to hip while balancing\n3. Lower with control\n4. Repeat on left side\n5. Maintain rigid torso throughout",
                tips = "Prevent hip rotation: Squeeze glutes. Combines core stability with upper body strength :cite[7]",
                category = "Abs"
            ),
            Exercise(
                id = 60,
                name = "Woodchoppers",
                isCompound = true,
                primaryMuscle = "Obliques",
                secondaryMuscles = "Shoulders,Back",
                equipment = "Cable Machine/Dumbbell",
                difficulty = "Intermediate",
                instructions = "1. Attach handle to high cable\n2. Stand sideways to machine\n3. Grab handle with both hands\n4. Pull diagonally across body to opposite knee\n5. Control return to start position",
                tips = "Initiate movement from core rotation. For fat burning: High reps (15-20/side) :cite[7]",
                category = "Abs"
            ),
            Exercise(
                id = 61,
                name = "Plank with Arm Raise",
                isCompound = true,
                primaryMuscle = "Core",
                secondaryMuscles = "Shoulders,Back",
                equipment = "None",
                difficulty = "Intermediate",
                instructions = "1. Assume forearm plank position\n2. Lift right arm straight forward\n3. Hold for 3-5 seconds\n4. Lower and repeat with left arm\n5. Maintain level hips throughout",
                tips = "Increases core activation by 30% vs standard plank. Regression: Knee plank position :cite[7]",
                category = "Abs"
            ),
            Exercise(
                id = 62,
                name = "Switching Mountain Climbers",
                isCompound = true,
                primaryMuscle = "Abs",
                secondaryMuscles = "Hip Flexors,Shoulders",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Start in high plank position\n2. Bring right knee toward right elbow\n3. Return to plank\n4. Bring left knee toward left elbow\n5. Alternate at controlled pace",
                tips = "Engages entire core when done slowly. For cardio: Increase speed after mastering form :cite[9]",
                category = "Abs"
            ),
            Exercise(
                id = 63,
                name = "Russian Twists",
                isCompound = true,
                primaryMuscle = "Obliques",
                secondaryMuscles = "Rectus Abdominis",
                equipment = "Medicine Ball/Dumbbell",
                difficulty = "Beginner",
                instructions = "1. Sit with knees bent, feet lifted\n2. Lean back 45° with straight spine\n3. Hold weight at chest level\n4. Rotate torso side to side\n5. Keep feet stationary",
                tips = "Advanced: Straighten legs. Focus on rotation from ribs not arms :cite[9]",
                category = "Abs"
            ),
            Exercise(
                id = 64,
                name = "Lying Leg Flutters",
                isCompound = false,
                primaryMuscle = "Lower Abs",
                secondaryMuscles = "Hip Flexors",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Lie face up with hands under glutes\n2. Lift legs 6 inches off floor\n3. Flutter legs in small, rapid motions\n4. Keep lower back pressed to floor\n5. Maintain continuous tension",
                tips = "Prevent back arch: Exhale and draw navel toward spine. 30-45 second sets :cite[9]",
                category = "Abs"
            ),
            Exercise(
                id = 65,
                name = "Dead Bug",
                isCompound = false,
                primaryMuscle = "Transverse Abdominis",
                secondaryMuscles = "",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Lie face up with knees over hips\n2. Extend arms toward ceiling\n3. Slowly lower right arm and left leg\n4. Stop before arching lower back\n5. Return and alternate sides",
                tips = "Most effective when done slowly (4s/rep). Essential for spinal stabilization :cite[4]",
                category = "Abs"
            ),
            Exercise(
                id = 66,
                name = "Cable Crunch",
                isCompound = false,
                primaryMuscle = "Upper Abs",
                secondaryMuscles = "Obliques",
                equipment = "Cable Machine",
                difficulty = "Intermediate",
                instructions = "1. Kneel below high cable with rope attachment\n2. Grab rope beside head\n3. Contract abs to curl torso downward\n4. Squeeze at bottom position\n5. Slowly return to start",
                tips = "Isolate abs: Keep hips stationary. Use 15-25 rep range for hypertrophy",
                category = "Abs"
            ),
            Exercise(
                id = 67,
                name = "Plank Side-to-Side",
                isCompound = false,
                primaryMuscle = "Obliques",
                secondaryMuscles = "Transverse Abdominis",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Assume forearm plank position\n2. Shift weight to left forearm\n3. Drop hips toward left side\n4. Return to center\n5. Repeat to right side",
                tips = "Enhances oblique definition. Keep shoulders stacked over elbows :cite[9]",
                category = "Abs"
            ),
            Exercise(
                id = 68,
                name = "Reverse Crunch",
                isCompound = false,
                primaryMuscle = "Lower Abs",
                secondaryMuscles = "",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Lie face up with knees bent 90°\n2. Place hands at sides or under sacrum\n3. Roll pelvis upward lifting hips off floor\n4. Slowly lower without touching heels down\n5. Maintain tension in lower abs",
                tips = "Advanced: Hold medicine ball between knees. Focus on pelvic tilt initiation",
                category = "Abs"
            ),
            Exercise(
                id = 69,
                name = "Bird Dog",
                isCompound = false,
                primaryMuscle = "Core",
                secondaryMuscles = "Lower Back",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "1. Start on hands and knees\n2. Extend right arm and left leg simultaneously\n3. Keep hips parallel to floor\n4. Hold for 2-3 seconds\n5. Alternate sides",
                tips = "Improves balance and coordination. Critical for preventing low back pain :cite[5]",
                category = "Abs"
            )
        )
    }
}
