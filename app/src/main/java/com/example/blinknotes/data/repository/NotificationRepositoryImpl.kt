package com.example.blinknotes.data.repository

import com.example.blinknotes.data.model.NotificationBody
import com.example.blinknotes.data.model.SendMessageDto
import com.example.blinknotes.data.remote.FcmApi
import com.example.blinknotes.domain.model.ActivityNotification
import com.example.blinknotes.domain.model.Notification
import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import android.util.Log

class NotificationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val fcmApi: FcmApi,
    private val userRepository: UserRepository
) : NotificationRepository {

    override suspend fun sendTagNotification(
        postId: String,
        senderId: String,
        content: String,
        taggedUserId: String
    ) {
        val sender = userRepository.getUserById(senderId) ?: return
        val taggedUser = userRepository.getUserById(taggedUserId) ?: return
        
        val notificationId = UUID.randomUUID().toString()
        val notification = mapOf(
            "id" to notificationId,
            "type" to NotificationType.TAGGED_IN_COMMENT.name,
            "title" to "Bạn được tag trong bình luận",
            "content" to "${sender.username} đã tag bạn trong một bình luận.",
            "userId" to senderId,
            "postId" to postId,
            "commentId" to "",
            "timestamp" to System.currentTimeMillis(),
            "receiverId" to taggedUserId,
            "isRead" to false,
            "imageUser" to sender.profileImage
        )

        db.collection("activity_notifications")
            .document(notificationId)
            .set(notification)
            .await()

        // Send FCM notification
        try {
            val messageDto = SendMessageDto(
                to = taggedUser.fcmToken.toString(),
                notification = NotificationBody(
                    title = "Bạn được tag trong bình luận",
                    body = "${sender.username} đã tag bạn trong một bình luận."
                )
            )
            fcmApi.sendMessage(messageDto)
        } catch (e: Exception) {
            // Handle FCM error
        }
    }

    override suspend fun getNotifications(userId: String): List<Notification> {
        return try {
            val result = db.collection("activity_notifications")
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Notification(
                    id = data["id"] as String,
                    type = NotificationType.valueOf(data["type"] as String),
                    title = data["title"] as String,
                    content = data["content"] as String,
                    userId = data["userId"] as String,
                    postId = data["postId"] as String,
                    commentId = data["commentId"] as String,
                    timestamp = data["timestamp"] as Long,
                    receiverId = data["receiverId"] as String,
                    isRead = data["isRead"] as Boolean,
                    imageUser = data["imageUser"] as String
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getActivityNotifications(userId: String): List<ActivityNotification> {
        return try {
            Log.d("NotificationRepositoryImpl", "Fetching activity notifications for user: $userId")
            val result = db.collection("activity_notifications")
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            Log.d("NotificationRepositoryImpl", "Found ${result.size()} notifications")
            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                try {
                    ActivityNotification(
                        id = doc.id,
                        type = data["type"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        userId = data["userId"] as? String ?: "",
                        postId = data["postId"] as? String ?: "",
                        commentId = data["commentId"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        imageUser = data["imageUser"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("NotificationRepositoryImpl", "Error parsing notification data: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationRepositoryImpl", "Error fetching activity notifications", e)
            emptyList()
        }
    }

    override suspend fun getLatestActivityNotification(userId: String): ActivityNotification? {
        return try {
            val result = db.collection("activity_notifications")
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            result.documents.firstOrNull()?.let { doc ->
                val data = doc.data ?: return@let null
                ActivityNotification(
                    id = doc.id,
                    type = data["type"] as String,
                    title = data["title"] as String,
                    content = data["content"] as String,
                    userId = data["userId"] as String,
                    postId = data["postId"] as String,
                    commentId = data["commentId"] as String,
                    timestamp = data["timestamp"] as Long,
                    imageUser = data["imageUser"] as String
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            db.collection("activity_notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun markActivityNotificationAsRead(notificationId: String): Boolean {
        return try {
            db.collection("activity_notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            db.collection("activity_notifications")
                .document(notificationId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUnreadNotificationCount(userId: String): Int {
        return try {
            val result = db.collection("activity_notifications")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            result.size()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun sendNotification(
        type: String,
        title: String,
        content: String,
        userId: String,
        postId: String?,
        commentId: String?,
        receiverId: String,
        imageUser: String?
    ) {
        val sender = userRepository.getUserById(userId) ?: return
        val receiver = userRepository.getUserById(receiverId) ?: return

        val notificationId = UUID.randomUUID().toString()
        val notification = mapOf(
            "id" to notificationId,
            "type" to type,
            "title" to title,
            "content" to content,
            "userId" to userId,
            "postId" to (postId ?: ""),
            "commentId" to (commentId ?: ""),
            "timestamp" to System.currentTimeMillis(),
            "receiverId" to receiverId,
            "isRead" to false,
            "imageUser" to (imageUser ?: sender.profileImage)
        )

        db.collection("activity_notifications")
            .document(notificationId)
            .set(notification)
            .await()

        // Send FCM notification
        try {
            val messageDto = SendMessageDto(
                to = receiver.fcmToken.toString(),
                notification = NotificationBody(
                    title = title,
                    body = content
                )
            )
            fcmApi.sendMessage(messageDto)
        } catch (e: Exception) {
            // Handle FCM error
        }
    }
} 