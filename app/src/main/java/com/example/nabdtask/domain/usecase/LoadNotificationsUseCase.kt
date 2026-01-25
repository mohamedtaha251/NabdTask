package com.example.nabdtask.domain.usecase

import com.example.nabdtask.domain.model.LocalNotification
import com.example.nabdtask.domain.repository.NotificationsRepository

class LoadNotificationsUseCase(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(): List<LocalNotification> {
        return repository.loadNotifications()
    }
}
