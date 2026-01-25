package com.example.nabdtask.domain.usecase

import com.example.nabdtask.domain.model.LocalNotification
import com.example.nabdtask.domain.notification.NotificationScheduler

class ScheduleNotificationUseCase(
    private val scheduler: NotificationScheduler
) {
    operator fun invoke(notification: LocalNotification) {
        scheduler.schedule(notification)
    }
}
