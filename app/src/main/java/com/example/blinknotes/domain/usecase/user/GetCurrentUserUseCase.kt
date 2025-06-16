package com.example.blinknotes.domain.usecase.user

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User? {
        return userRepository.getCurrentUser()
    }
} 