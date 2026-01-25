package com.example.nabdtask

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    const val CHANNEL_ID = "local_notifications"
    private const val TAG = "local_notification"
    private const val KEY_ID = "id"
    private const val KEY_TITLE = "title"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Local Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, notification: LocalNotification) {
        createChannel(context)
        val data = Data.Builder()
            .putInt(KEY_ID, notification.id)
            .putString(KEY_TITLE, notification.title)
            .build()
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(notification.timeInSeconds, TimeUnit.SECONDS)
            .setInputData(data)
            .addTag(TAG)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(notification), androidx.work.ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, notification: LocalNotification) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(notification))
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notification.id)
    }

    fun cancelAll(context: Context, notifications: List<LocalNotification>) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    fun parseInputId(data: Data): Int {
        return data.getInt(KEY_ID, 0)
    }

    fun parseInputTitle(data: Data): String {
        return data.getString(KEY_TITLE) ?: "Notification"
    }

    private fun uniqueName(notification: LocalNotification): String {
        return "local_notification_${notification.id}"
    }
}
