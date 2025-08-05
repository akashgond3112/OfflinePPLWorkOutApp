package com.example.offlinepplworkoutapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.offlinepplworkoutapp.data.database.PPLWorkoutDatabase
import com.example.offlinepplworkoutapp.data.repository.WorkoutRepository
import com.example.offlinepplworkoutapp.ui.screens.MainScreen
import com.example.offlinepplworkoutapp.ui.theme.OfflinePPLWorkOutAppTheme
import com.example.offlinepplworkoutapp.util.NotificationHelper

private const val IS_DEBUG_MODE = true

class MainActivity : ComponentActivity() {

    private lateinit var database: PPLWorkoutDatabase
    private lateinit var repository: WorkoutRepository
    private lateinit var notificationHelper: NotificationHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        println("🔔 NOTIFICATION: Permission ${if (isGranted) "granted" else "denied"}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = PPLWorkoutDatabase.getDatabase(this)
        repository = WorkoutRepository(
            workoutDayDao = database.workoutDayDao(),
            workoutEntryDao = database.workoutEntryDao(),
            setEntryDao = database.setEntryDao(),
            workoutTemplateDao = database.workoutTemplateDao(),
            templateExerciseDao = database.templateExerciseDao()
        )

        notificationHelper = NotificationHelper(this)
        notificationHelper.createChannels()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            OfflinePPLWorkOutAppTheme {
                MainScreen(repository = repository)
            }
        }
    }
}
