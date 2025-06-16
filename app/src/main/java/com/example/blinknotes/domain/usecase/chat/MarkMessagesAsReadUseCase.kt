package com.example.blinknotes.domain.usecase.chat

import com.example.blinknotes.domain.repository.ChatRepository
import javax.inject.Inject

class MarkMessagesAsReadUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(senderId: String, receiverId: String) {
        repository.markMessagesAsRead(senderId, receiverId)
    }
} 