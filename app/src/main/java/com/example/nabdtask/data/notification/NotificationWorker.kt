package com.example.nabdtask.data.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result {
        val scheduler = WorkManagerNotificationScheduler(applicationContext)
        scheduler.createChannel()
        val id = WorkManagerNotificationScheduler.parseInputId(inputData)
        val title = WorkManagerNotificationScheduler.parseInputTitle(inputData)
        val notification = NotificationCompat.Builder(applicationContext, WorkManagerNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(id, notification)
        return Result.success()
    }
}
