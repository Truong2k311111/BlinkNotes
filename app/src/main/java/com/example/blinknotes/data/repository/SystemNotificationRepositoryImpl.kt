package com.example.blinknotes.data.repository

import com.example.blinknotes.data.model.NotificationBody
import com.example.blinknotes.data.model.SendMessageDto
import com.example.blinknotes.data.remote.FcmApi
import com.example.blinknotes.domain.model.SystemNotification
import com.example.blinknotes.domain.repository.SystemNotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SystemNotificationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val fcmApi: FcmApi
) : SystemNotificationRepository {

    override suspend fun createSystemNotification(notification: SystemNotification) {
        db.collection("system_notifications")
            .document(notification.id)
            .set(notification)
            .await()

        // Send FCM notification to admin users
        db.collection("users")
            .whereEqualTo("isAdmin", true)
            .get()
            .await()
            .documents
            .forEach { adminDoc ->
                val adminToken = adminDoc.getString("fcmToken")
                if (!adminToken.isNullOrBlank()) {
                    val messageDto = SendMessageDto(
                        to = adminToken,
                        notification = NotificationBody(
                            title = notification.title,
                            body = notification.content
                        )
                    )
                    fcmApi.sendMessage(messageDto)
                }
            }
    }

    override suspend fun getSystemNotifications(): List<SystemNotification> {
        return db.collection("system_notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(SystemNotification::class.java)
            }
    }

    override suspend fun markSystemNotificationAsRead(notificationId: String): Boolean {
        return try {
            db.collection("system_notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteSystemNotification(notificationId: String): Boolean {
        return try {
            db.collection("system_notifications")
                .document(notificationId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUnreadSystemNotificationCount(): Int {
        return db.collection("system_notifications")
            .whereEqualTo("isRead", false)
            .get()
            .await()
            .size()
    }

    override suspend fun getLatestSystemNotification(): SystemNotification? {
        return db.collection("system_notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(SystemNotification::class.java)
    }
} 