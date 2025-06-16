package com.example.blinknotes.domain.usecase.auth

import com.example.blinknotes.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.resetPassword(email)
    }
} 