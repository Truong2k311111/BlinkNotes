package com.example.blinknotes.data.repository

import android.util.Log
import com.example.blinknotes.data.model.NotificationBody
import com.example.blinknotes.data.model.SendMessageDto
import com.example.blinknotes.data.remote.FcmApi
import com.example.blinknotes.domain.model.Message
import com.example.blinknotes.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val fcmApi: FcmApi
) : ChatRepository {

    override suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        imageUrls: List<String>,
        contentSendImage: String
    ): Result<Message> = try {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val message = Message(
            id = messageId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            contentSendImage = contentSendImage,
            imageUrls = imageUrls,
            isRead = false,
            timestamp = timestamp
        )
        firestore.collection("contentchat")
            .document(messageId)
            .set(message)
            .await()
        val receiverDoc = firestore.collection("users").document(receiverId).get().await()
        val receiverToken = receiverDoc.getString("fcmToken")

        if (receiverToken != null) {
            val senderDoc = firestore.collection("users").document(senderId).get().await()
            val senderName = senderDoc.getString("username") ?: "Unknown"

            val messageDto = SendMessageDto(
                to = receiverToken,
                notification = NotificationBody(
                    title = "Bạn có tin nhắn mới từ $senderName",
                    body = if (content.isNotBlank()) content else contentSendImage
                )
            )

            fcmApi.sendMessage(messageDto)
        }

        Result.success(message)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMessages(currentUserId: String, otherUserId: String): List<Message> {
        return try {
            val messages = mutableListOf<Message>()

            // Tin nhắn do currentUser gửi cho otherUser
            val sentMessages = firestore.collection("contentchat")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", otherUserId)
                .get()
                .await()
                .toObjects(Message::class.java)

            // Tin nhắn do otherUser gửi cho currentUser
            val receivedMessages = firestore.collection("contentchat")
                .whereEqualTo("senderId", otherUserId)
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .await()
                .toObjects(Message::class.java)

            messages.addAll(sentMessages)
            messages.addAll(receivedMessages)

            // Sắp xếp theo thời gian tăng dần để hiển thị đúng thứ tự hội thoại
            messages.sortedBy { it.timestamp }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Lỗi khi lấy tin nhắn", e)
            emptyList()
        }
    }


    override suspend fun markMessagesAsRead(senderId: String, receiverId: String) {
        val chatId = getChatId(senderId, receiverId)
        try {
            val messages = firestore.collection("contentchat")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            messages.documents.forEach { doc ->
                doc.reference.update("isRead", true).await()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun getUnreadMessageCount(userId: String): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        try {
            val chats = firestore.collection("contentchat")
                .whereArrayContains("participants", userId)
                .get()
                .await()

            for (chat in chats.documents) {
                val messages = chat.reference.collection("messages")
                    .whereEqualTo("receiverId", userId)
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()

                val otherUserId = (chat.get("participants") as List<String>)
                    .firstOrNull { it != userId }

                if (otherUserId != null) {
                    result[otherUserId] = messages.size()
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
        return result
    }

    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }
} 