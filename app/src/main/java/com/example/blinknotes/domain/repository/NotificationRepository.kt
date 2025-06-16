package com.example.blinknotes.domain.repository

import com.example.blinknotes.domain.model.ActivityNotification
import com.example.blinknotes.domain.model.Notification
import com.example.blinknotes.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun sendTagNotification(
        postId: String,
        senderId: String,
        content: String,
        taggedUserId: String
    )
    suspend fun getNotifications(userId: String): List<Notification>
    suspend fun getActivityNotifications(userId: String): List<ActivityNotification>
    suspend fun getLatestActivityNotification(userId: String): ActivityNotification?
    suspend fun markNotificationAsRead(notificationId: String): Boolean
    suspend fun markActivityNotificationAsRead(notificationId: String): Boolean
    suspend fun deleteNotification(notificationId: String): Boolean
    suspend fun getUnreadNotificationCount(userId: String): Int
    suspend fun sendNotification(
        type: String,
        title: String,
        content: String,
        userId: String,
        postId: String?,
        commentId: String?,
        receiverId: String,
        imageUser: String? = null
    )
} 