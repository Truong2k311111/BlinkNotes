package com.example.blinknotes.domain.usecase.user

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): User? {
        return userRepository.getUserById(userId)
    }

    suspend fun invokeByBlinkNotesId(blinkNotesId: String): User? {
        return userRepository.getUserByBlinkNotesId(blinkNotesId)
    }
}
