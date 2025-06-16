package com.example.blinknotes.domain.repository

import com.example.blinknotes.domain.model.SystemNotification

interface SystemNotificationRepository {
    suspend fun createSystemNotification(notification: SystemNotification)
    suspend fun getSystemNotifications(): List<SystemNotification>
    suspend fun markSystemNotificationAsRead(notificationId: String): Boolean
    suspend fun deleteSystemNotification(notificationId: String): Boolean
    suspend fun getUnreadSystemNotificationCount(): Int
    suspend fun getLatestSystemNotification(): SystemNotification?
} 