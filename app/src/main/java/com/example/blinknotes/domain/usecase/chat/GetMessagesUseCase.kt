package com.example.blinknotes.domain.usecase.chat

import com.example.blinknotes.domain.model.Message
import com.example.blinknotes.domain.repository.ChatRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(currentUserId: String, otherUserId: String): List<Message> {
        return repository.getMessages(currentUserId, otherUserId)
    }
} 