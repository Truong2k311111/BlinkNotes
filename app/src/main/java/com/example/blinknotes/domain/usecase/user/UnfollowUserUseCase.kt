package com.example.blinknotes.domain.usecase.user

import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class UnfollowUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(targetUserId: String) {
        userRepository.unfollowUser(targetUserId)
    }
} 