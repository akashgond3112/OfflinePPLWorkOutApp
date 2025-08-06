package com.example.offlinepplworkoutapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.offlinepplworkoutapp.MainActivity
import com.example.offlinepplworkoutapp.R

/**
 * Helper class for creating and showing notifications in the app
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_WORKOUT = "workout_notifications"
        const val NOTIFICATION_ID_REST_TIMER = 1001
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Initialize notification channels for the app.
     * This should be called once at app startup.
     */
    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Get default notification sound
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // Set audio attributes for the channel
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val workoutChannel = NotificationChannel(
                CHANNEL_ID_WORKOUT,
                "Workout Notifications",
                NotificationManager.IMPORTANCE_HIGH // HIGH importance for heads-up display
            ).apply {
                description = "Notifications for workout timers and events"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250) // Vibration pattern
                setSound(defaultSoundUri, audioAttributes) // Set sound with attributes
                setShowBadge(true)
                enableLights(true)
                lightColor = context.resources.getColor(R.color.purple_500, null)
            }

            notificationManager.createNotificationChannel(workoutChannel)
            println("🔔 NOTIFICATION: Created notification channel: ${workoutChannel.id}")
        }
    }

    /**
     * Show a notification when rest timer reaches 1 minute
     */
    fun showRestTimerNotification(exerciseName: String) {
        println("🔔 NOTIFICATION: Preparing to show rest timer notification for $exerciseName")

        // Create an intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get default notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build the notification with heads-up display capability
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WORKOUT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Rest Timer Alert")
            .setContentText("You've been resting for 1 minute on $exerciseName")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Use alarm category for higher priority
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250)) // Add vibration pattern
            .setAutoCancel(true)
            // Make this a heads-up notification
            .setFullScreenIntent(pendingIntent, true)
            .build()

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID_REST_TIMER, notification)
        println("🔔 NOTIFICATION: Rest timer notification sent with ID: $NOTIFICATION_ID_REST_TIMER")
    }

    /**
     * Cancel any active rest timer notifications
     */
    fun cancelRestTimerNotification() {
        notificationManager.cancel(NOTIFICATION_ID_REST_TIMER)
        println("🔔 NOTIFICATION: Rest timer notification cancelled")
    }
}
