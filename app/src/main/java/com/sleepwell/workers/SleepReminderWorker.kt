package com.sleepwell.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sleepwell.R
import com.sleepwell.ui.main.MainActivity
import com.sleepwell.utils.Constants

class SleepReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            showSleepReminderNotification()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showSleepReminderNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            Constants.NOTIFICATION_CHANNEL_SLEEP_ID
        )
            .setSmallIcon(R.drawable.ic_sleep)
            .setContentTitle(applicationContext.getString(R.string.notification_sleep_title))
            .setContentText(applicationContext.getString(R.string.notification_sleep_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constants.NOTIFICATION_ID_SLEEP, notification)
    }
}
