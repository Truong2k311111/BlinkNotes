package com.example.blinknotes.domain.repository

import com.example.blinknotes.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        imageUrls: List<String> = emptyList(),
        contentSendImage: String = ""
    ): Result<Message>

    suspend fun getMessages(currentUserId: String, otherUserId: String): List<Message>

    suspend fun markMessagesAsRead(senderId: String, receiverId: String)

    suspend fun getUnreadMessageCount(userId: String): Map<String, Int>
} 