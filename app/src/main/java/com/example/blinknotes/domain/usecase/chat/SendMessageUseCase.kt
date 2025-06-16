package com.example.blinknotes.domain.usecase.chat

import com.example.blinknotes.domain.model.Message
import com.example.blinknotes.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        senderId: String,
        receiverId: String,
        content: String,
        imageUrls: List<String> = emptyList(),
        contentSendImage: String = ""
    ): Result<Message> {
        return repository.sendMessage(
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            imageUrls = imageUrls,
            contentSendImage = contentSendImage
        )
    }
} 