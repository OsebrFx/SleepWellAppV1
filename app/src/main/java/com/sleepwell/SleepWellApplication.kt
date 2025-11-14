package com.sleepwell

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.sleepwell.utils.Constants

class SleepWellApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize WorkManager
        WorkManager.initialize(this, workManagerConfiguration)

        // Create notification channels
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Sleep Reminder Channel
            val sleepChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SLEEP_ID,
                Constants.NOTIFICATION_CHANNEL_SLEEP_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels pour aller dormir"
                enableVibration(true)
                enableLights(true)
            }

            // Wake Up Reminder Channel
            val wakeupChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_WAKEUP_ID,
                Constants.NOTIFICATION_CHANNEL_WAKEUP_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels de réveil"
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(sleepChannel)
            notificationManager.createNotificationChannel(wakeupChannel)
        }
    }

    companion object {
        lateinit var instance: SleepWellApplication
            private set
    }
}
