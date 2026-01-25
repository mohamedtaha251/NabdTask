package com.example.nabdtask.domain.usecase

import com.example.nabdtask.domain.notification.NotificationScheduler

class CancelAllNotificationsUseCase(
    private val scheduler: NotificationScheduler
) {
    operator fun invoke() {
        scheduler.cancelAll()
    }
}
