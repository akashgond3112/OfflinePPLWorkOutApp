package com.example.offlinepplworkoutapp.data

import com.example.offlinepplworkoutapp.data.entity.Exercise

/**
 * Enhanced PPL Exercise Library with rich metadata
 * This contains all 34 exercises with detailed information for better user experience
 */
object ExerciseData {

    fun getPPLExercises(): List<Exercise> {
        return listOf(
            // Push Day 1 (Monday) - Exercises 1-5
            Exercise(
                id = 1,
                name = "Barbell Bench Press",
                isCompound = true,
                primaryMuscle = "Chest",
                secondaryMuscles = "Triceps,Front Delts",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Lie flat on bench with eyes under the bar\n2. Grip bar slightly wider than shoulder width\n3. Unrack and lower bar to chest with control\n4. Press bar up explosively to starting position",
                tips = "Keep shoulder blades retracted throughout movement. Don't bounce bar off chest. Maintain tight core.",
                category = "Push"
            ),
            Exercise(
                id = 2,
                name = "Standing Overhead Press",
                isCompound = true,
                primaryMuscle = "Shoulders",
                secondaryMuscles = "Triceps,Upper Chest,Core",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Stand with feet shoulder-width apart\n2. Grip bar at shoulder width, rest on front delts\n3. Press bar straight up overhead\n4. Lower with control to starting position",
                tips = "Keep core tight. Don't arch back excessively. Bar path should be straight up.",
                category = "Push"
            ),
            Exercise(
                id = 3,
                name = "Incline Dumbbell Press",
                isCompound = true,
                primaryMuscle = "Upper Chest",
                secondaryMuscles = "Front Delts,Triceps",
                equipment = "Dumbbells",
                difficulty = "Beginner",
                instructions = "1. Set bench to 30-45 degree incline\n2. Hold dumbbells at chest level\n3. Press weights up and slightly together\n4. Lower with control to starting position",
                tips = "Don't press dumbbells straight up - bring them together at top. Control the negative.",
                category = "Push"
            ),
            Exercise(
                id = 4,
                name = "Dumbbell Lateral Raise",
                isCompound = false,
                primaryMuscle = "Side Delts",
                secondaryMuscles = "Front Delts,Rear Delts",
                equipment = "Dumbbells",
                difficulty = "Beginner",
                instructions = "1. Stand with dumbbells at sides\n2. Raise arms out to sides until parallel to floor\n3. Lower with control\n4. Keep slight bend in elbows throughout",
                tips = "Lead with pinkies, not thumbs. Don't swing or use momentum. Focus on mind-muscle connection.",
                category = "Push"
            ),
            Exercise(
                id = 5,
                name = "Cable Triceps Pushdown",
                isCompound = false,
                primaryMuscle = "Triceps",
                secondaryMuscles = "",
                equipment = "Cable Machine",
                difficulty = "Beginner",
                instructions = "1. Stand at cable machine with rope or bar attachment\n2. Keep elbows at sides\n3. Push weight down until arms are fully extended\n4. Return to starting position with control",
                tips = "Keep elbows stationary. Focus on triceps contraction at bottom. Don't lean forward.",
                category = "Push"
            ),

            // Pull Day 1 (Tuesday) - Exercises 6-11
            Exercise(
                id = 6,
                name = "Deadlift",
                isCompound = true,
                primaryMuscle = "Posterior Chain",
                secondaryMuscles = "Traps,Lats,Glutes,Hamstrings,Core",
                equipment = "Barbell",
                difficulty = "Advanced",
                instructions = "1. Stand with bar over mid-foot\n2. Bend down and grip bar outside legs\n3. Lift chest, engage lats\n4. Drive through heels and pull bar up legs\n5. Stand tall, then reverse movement",
                tips = "Keep bar close to body. Don't round back. Drive hips forward at top.",
                category = "Pull"
            ),
            Exercise(
                id = 7,
                name = "Pull-Ups or Lat Pulldowns",
                isCompound = true,
                primaryMuscle = "Lats",
                secondaryMuscles = "Rhomboids,Middle Traps,Biceps",
                equipment = "Pull-up Bar/Cable Machine",
                difficulty = "Intermediate",
                instructions = "1. Hang from bar with overhand grip\n2. Pull body up until chin clears bar\n3. Lower with control to full hang\n4. If using cable, pull bar to upper chest",
                tips = "Don't swing or kip. Focus on pulling with lats, not arms. Full range of motion.",
                category = "Pull"
            ),
            Exercise(
                id = 8,
                name = "Bent-Over Barbell Row",
                isCompound = true,
                primaryMuscle = "Middle Traps",
                secondaryMuscles = "Rhomboids,Lats,Rear Delts,Biceps",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Hinge at hips with slight knee bend\n2. Hold bar with overhand grip\n3. Pull bar to lower chest/upper abdomen\n4. Lower with control",
                tips = "Keep torso angle consistent. Don't use momentum. Squeeze shoulder blades together.",
                category = "Pull"
            ),
            Exercise(
                id = 9,
                name = "Face Pull",
                isCompound = false,
                primaryMuscle = "Rear Delts",
                secondaryMuscles = "Middle Traps,Rhomboids",
                equipment = "Cable Machine",
                difficulty = "Beginner",
                instructions = "1. Set cable at face height with rope attachment\n2. Pull rope to face, separating hands\n3. Focus on rear delt contraction\n4. Return with control",
                tips = "Keep elbows high. Don't use too much weight. Focus on form over weight.",
                category = "Pull"
            ),
            Exercise(
                id = 10,
                name = "Barbell Biceps Curl",
                isCompound = false,
                primaryMuscle = "Biceps",
                secondaryMuscles = "Forearms",
                equipment = "Barbell",
                difficulty = "Beginner",
                instructions = "1. Stand with bar at arm's length\n2. Keep elbows at sides\n3. Curl bar up to chest level\n4. Lower with control",
                tips = "Don't swing or use momentum. Keep elbows stationary. Control the negative.",
                category = "Pull"
            ),
            Exercise(
                id = 11,
                name = "Hammer Curl",
                isCompound = false,
                primaryMuscle = "Biceps",
                secondaryMuscles = "Forearms,Brachialis",
                equipment = "Dumbbells",
                difficulty = "Beginner",
                instructions = "1. Hold dumbbells with neutral grip (palms facing each other)\n2. Keep elbows at sides\n3. Curl weights up\n4. Lower with control",
                tips = "Maintain neutral grip throughout. Don't swing weights. Focus on bicep contraction.",
                category = "Pull"
            ),

            // Legs Day 1 (Wednesday) - Exercises 12-16
            Exercise(
                id = 12,
                name = "Back Squat",
                isCompound = true,
                primaryMuscle = "Quadriceps",
                secondaryMuscles = "Glutes,Hamstrings,Core",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Position bar on upper traps\n2. Stand with feet shoulder-width apart\n3. Descend by sitting back and down\n4. Drive through heels to stand",
                tips = "Keep chest up and knees tracking over toes. Go to parallel or below. Don't let knees cave in.",
                category = "Legs"
            ),
            Exercise(
                id = 13,
                name = "Romanian Deadlift",
                isCompound = true,
                primaryMuscle = "Hamstrings",
                secondaryMuscles = "Glutes,Lower Back",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Hold bar at hip level\n2. Keep slight knee bend\n3. Hinge at hips, lowering bar\n4. Drive hips forward to return",
                tips = "Keep bar close to legs. Feel stretch in hamstrings. Don't round back.",
                category = "Legs"
            ),
            Exercise(
                id = 14,
                name = "Leg Press",
                isCompound = true,
                primaryMuscle = "Quadriceps",
                secondaryMuscles = "Glutes,Hamstrings",
                equipment = "Leg Press Machine",
                difficulty = "Beginner",
                instructions = "1. Sit in leg press machine\n2. Place feet shoulder-width apart on platform\n3. Lower weight until knees are at 90 degrees\n4. Press through heels to extend legs",
                tips = "Don't lock knees completely. Keep core engaged. Control both directions.",
                category = "Legs"
            ),
            Exercise(
                id = 15,
                name = "Lying Leg Curl",
                isCompound = false,
                primaryMuscle = "Hamstrings",
                secondaryMuscles = "",
                equipment = "Leg Curl Machine",
                difficulty = "Beginner",
                instructions = "1. Lie face down on leg curl machine\n2. Position ankles under pads\n3. Curl heels toward glutes\n4. Lower with control",
                tips = "Don't lift hips off pad. Full range of motion. Control the negative.",
                category = "Legs"
            ),
            Exercise(
                id = 16,
                name = "Seated Calf Raise",
                isCompound = false,
                primaryMuscle = "Calves",
                secondaryMuscles = "",
                equipment = "Calf Raise Machine",
                difficulty = "Beginner",
                instructions = "1. Sit in calf raise machine\n2. Position balls of feet on platform\n3. Raise heels as high as possible\n4. Lower to full stretch",
                tips = "Get full range of motion. Pause at top. Feel stretch at bottom.",
                category = "Legs"
            ),

            // Push Day 2 (Thursday) - Exercises 17-22
            Exercise(
                id = 17,
                name = "Standing Overhead Press",
                isCompound = true,
                primaryMuscle = "Shoulders",
                secondaryMuscles = "Triceps,Upper Chest,Core",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Stand with feet shoulder-width apart\n2. Grip bar at shoulder width, rest on front delts\n3. Press bar straight up overhead\n4. Lower with control to starting position",
                tips = "Keep core tight. Don't arch back excessively. Bar path should be straight up.",
                category = "Push"
            ),
            Exercise(
                id = 18,
                name = "Incline Barbell Press",
                isCompound = true,
                primaryMuscle = "Upper Chest",
                secondaryMuscles = "Front Delts,Triceps",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Set bench to 30-45 degree incline\n2. Lie back and grip bar slightly wider than shoulders\n3. Lower bar to upper chest\n4. Press bar up and slightly back",
                tips = "Don't touch bar to neck. Keep shoulder blades retracted. Control the descent.",
                category = "Push"
            ),
            Exercise(
                id = 19,
                name = "Weighted Dips",
                isCompound = true,
                primaryMuscle = "Lower Chest",
                secondaryMuscles = "Triceps,Front Delts",
                equipment = "Dip Station",
                difficulty = "Intermediate",
                instructions = "1. Support body on dip bars\n2. Lower body by bending elbows\n3. Descend until shoulders are below elbows\n4. Press back up to starting position",
                tips = "Lean slightly forward for chest emphasis. Don't go too deep if shoulders hurt. Control the movement.",
                category = "Push"
            ),
            Exercise(
                id = 20,
                name = "Cable Lateral Raise",
                isCompound = false,
                primaryMuscle = "Side Delts",
                secondaryMuscles = "Front Delts",
                equipment = "Cable Machine",
                difficulty = "Beginner",
                instructions = "1. Stand sideways to cable machine\n2. Grab handle with far hand\n3. Raise arm out to side until parallel to floor\n4. Lower with control",
                tips = "Use one arm at a time for better control. Don't swing or use momentum. Feel the burn.",
                category = "Push"
            ),
            Exercise(
                id = 21,
                name = "Pec Deck or Dumbbell Fly",
                isCompound = false,
                primaryMuscle = "Chest",
                secondaryMuscles = "Front Delts",
                equipment = "Pec Deck/Dumbbells",
                difficulty = "Beginner",
                instructions = "1. Sit in pec deck or lie on bench with dumbbells\n2. Bring arms together in wide arc\n3. Squeeze chest muscles at peak contraction\n4. Return to starting position with control",
                tips = "Focus on chest squeeze, not weight moved. Keep slight bend in elbows. Feel the stretch.",
                category = "Push"
            ),
            Exercise(
                id = 22,
                name = "Overhead Cable Triceps Extension",
                isCompound = false,
                primaryMuscle = "Triceps",
                secondaryMuscles = "",
                equipment = "Cable Machine",
                difficulty = "Beginner",
                instructions = "1. Face away from cable machine\n2. Hold rope overhead with arms extended\n3. Lower rope behind head by bending elbows\n4. Extend arms back to starting position",
                tips = "Keep elbows stationary and pointing forward. Don't let elbows flare out. Full range of motion.",
                category = "Push"
            ),

            // Pull Day 2 (Friday) - Exercises 23-28
            Exercise(
                id = 23,
                name = "Pendlay or Bent-Over Row",
                isCompound = true,
                primaryMuscle = "Middle Traps",
                secondaryMuscles = "Rhomboids,Lats,Rear Delts,Biceps",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Bend over with torso parallel to floor\n2. Grip bar with overhand grip\n3. Pull bar explosively to lower chest\n4. Lower bar to floor with control",
                tips = "Start each rep from dead stop. Keep torso parallel. Don't use leg drive.",
                category = "Pull"
            ),
            Exercise(
                id = 24,
                name = "Weighted Pull-Ups or Wide-Grip Lat Pulldown",
                isCompound = true,
                primaryMuscle = "Lats",
                secondaryMuscles = "Rhomboids,Middle Traps,Biceps",
                equipment = "Pull-up Bar/Cable Machine",
                difficulty = "Advanced",
                instructions = "1. Use wide grip on bar\n2. Pull body up or bar down to upper chest\n3. Focus on lat engagement\n4. Lower with control",
                tips = "Think about pulling elbows down and back. Don't use biceps primarily. Full range of motion.",
                category = "Pull"
            ),
            Exercise(
                id = 25,
                name = "Dumbbell Shrug",
                isCompound = false,
                primaryMuscle = "Upper Traps",
                secondaryMuscles = "",
                equipment = "Dumbbells",
                difficulty = "Beginner",
                instructions = "1. Hold dumbbells at sides\n2. Shrug shoulders straight up\n3. Hold peak contraction briefly\n4. Lower shoulders with control",
                tips = "Don't roll shoulders. Straight up and down motion. Focus on trap contraction.",
                category = "Pull"
            ),
            Exercise(
                id = 26,
                name = "Face Pull",
                isCompound = false,
                primaryMuscle = "Rear Delts",
                secondaryMuscles = "Middle Traps,Rhomboids",
                equipment = "Cable Machine",
                difficulty = "Beginner",
                instructions = "1. Set cable at face height with rope attachment\n2. Pull rope to face, separating hands\n3. Focus on rear delt contraction\n4. Return with control",
                tips = "Keep elbows high. Don't use too much weight. Focus on form over weight.",
                category = "Pull"
            ),
            Exercise(
                id = 27,
                name = "EZ-Bar Biceps Curl",
                isCompound = false,
                primaryMuscle = "Biceps",
                secondaryMuscles = "Forearms",
                equipment = "EZ-Bar",
                difficulty = "Beginner",
                instructions = "1. Hold EZ-bar with underhand grip\n2. Keep elbows at sides\n3. Curl bar up to chest level\n4. Lower with control",
                tips = "EZ-bar is easier on wrists than straight bar. Don't swing. Control the negative.",
                category = "Pull"
            ),
            Exercise(
                id = 28,
                name = "Reverse Grip or Preacher Curl",
                isCompound = false,
                primaryMuscle = "Biceps",
                secondaryMuscles = "Forearms,Brachialis",
                equipment = "Barbell/Dumbbells",
                difficulty = "Beginner",
                instructions = "1. Use overhand grip for reverse curl or preacher bench\n2. Curl weight up focusing on bicep contraction\n3. Lower with control\n4. Don't use momentum",
                tips = "Reverse curls target brachialis more. Preacher curls provide constant tension. Control the weight.",
                category = "Pull"
            ),

            // Legs Day 2 (Saturday) - Exercises 29-34
            Exercise(
                id = 29,
                name = "Front Squat",
                isCompound = true,
                primaryMuscle = "Quadriceps",
                secondaryMuscles = "Glutes,Core",
                equipment = "Barbell",
                difficulty = "Advanced",
                instructions = "1. Position bar on front delts in front rack position\n2. Keep elbows high and chest up\n3. Descend into squat\n4. Drive through heels to stand",
                tips = "More quad-focused than back squat. Keep torso upright. May need lighter weight than back squat.",
                category = "Legs"
            ),
            Exercise(
                id = 30,
                name = "Bulgarian Split Squat",
                isCompound = true,
                primaryMuscle = "Quadriceps",
                secondaryMuscles = "Glutes,Hamstrings",
                equipment = "Dumbbells",
                difficulty = "Intermediate",
                instructions = "1. Stand in lunge position with rear foot elevated\n2. Lower into lunge by bending front leg\n3. Keep most weight on front leg\n4. Drive through front heel to return",
                tips = "Don't put too much weight on back foot. Keep torso upright. Full range of motion.",
                category = "Legs"
            ),
            Exercise(
                id = 31,
                name = "Barbell Hip Thrust",
                isCompound = true,
                primaryMuscle = "Glutes",
                secondaryMuscles = "Hamstrings",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "1. Sit with back against bench, bar across hips\n2. Drive through heels to lift hips up\n3. Squeeze glutes at top\n4. Lower with control",
                tips = "Focus on glute contraction. Don't overextend lower back. Use pad for comfort.",
                category = "Legs"
            ),
            Exercise(
                id = 32,
                name = "Leg Extension",
                isCompound = false,
                primaryMuscle = "Quadriceps",
                secondaryMuscles = "",
                equipment = "Leg Extension Machine",
                difficulty = "Beginner",
                instructions = "1. Sit in leg extension machine\n2. Position ankles under pads\n3. Extend legs until straight\n4. Lower with control",
                tips = "Don't lock knees aggressively. Control both directions. Feel quad contraction.",
                category = "Legs"
            ),
            Exercise(
                id = 33,
                name = "Seated or Lying Leg Curl",
                isCompound = false,
                primaryMuscle = "Hamstrings",
                secondaryMuscles = "",
                equipment = "Leg Curl Machine",
                difficulty = "Beginner",
                instructions = "1. Position yourself in leg curl machine\n2. Place ankles under pads\n3. Curl heels toward glutes\n4. Lower with control",
                tips = "Don't lift hips off pad. Full range of motion. Control the negative.",
                category = "Legs"
            ),
            Exercise(
                id = 34,
                name = "Standing Calf Raise",
                isCompound = false,
                primaryMuscle = "Calves",
                secondaryMuscles = "",
                equipment = "Calf Raise Machine",
                difficulty = "Beginner",
                instructions = "1. Stand in calf raise machine\n2. Position balls of feet on platform\n3. Raise heels as high as possible\n4. Lower to full stretch",
                tips = "Get full range of motion. Pause at top. Feel stretch at bottom. Don't bounce.",
                category = "Legs"
            )
        )
    }
}
