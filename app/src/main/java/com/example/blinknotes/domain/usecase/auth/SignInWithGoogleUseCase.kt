package com.example.blinknotes.domain.usecase.auth

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credential: AuthCredential): String {
        return authRepository.signInWithCredential(credential)
    }
} 