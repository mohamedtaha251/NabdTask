package com.example.nabdtask.domain.repository

import com.example.nabdtask.domain.model.LocalNotification

interface NotificationsRepository {
    suspend fun loadNotifications(): List<LocalNotification>
}
