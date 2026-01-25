package com.example.nabdtask.data.notification

import android.Manifest
import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val scheduler = WorkManagerNotificationScheduler(applicationContext)
        scheduler.createChannel()
        val id = WorkManagerNotificationScheduler.parseInputId(inputData)
        val title = WorkManagerNotificationScheduler.parseInputTitle(inputData)
        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                applicationContext,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, WorkManagerNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(id, notification)
        return Result.success()
    }
}
