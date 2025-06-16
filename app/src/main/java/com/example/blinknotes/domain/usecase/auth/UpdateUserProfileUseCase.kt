package com.example.blinknotes.domain.usecase.auth

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, bio: String, profileImageUrl: String?): User {
        return authRepository.updateUserProfile(username, bio, profileImageUrl)
    }
} 