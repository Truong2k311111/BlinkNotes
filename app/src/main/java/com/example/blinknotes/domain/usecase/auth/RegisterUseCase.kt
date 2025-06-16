package com.example.blinknotes.domain.usecase.auth

import com.example.blinknotes.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, username: String) {
        authRepository.register(email, password, username)
    }
} 