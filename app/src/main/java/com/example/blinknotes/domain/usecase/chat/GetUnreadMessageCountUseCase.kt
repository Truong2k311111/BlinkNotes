package com.example.blinknotes.domain.usecase.chat

import com.example.blinknotes.domain.repository.ChatRepository
import javax.inject.Inject

class GetUnreadMessageCountUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(userId: String): Map<String, Int> {
        return repository.getUnreadMessageCount(userId)
    }
} 