package com.example.nabdtask.domain.notification

import com.example.nabdtask.domain.model.LocalNotification

interface NotificationScheduler {
    fun createChannel()
    fun schedule(notification: LocalNotification)
    fun cancel(notification: LocalNotification)
    fun cancelAll()
}
