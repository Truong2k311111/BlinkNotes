package com.example.blinknotes.domain.repository

import com.example.blinknotes.domain.model.User
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(email: String, password: String, username: String): User
    suspend fun resetPassword(email: String)
    suspend fun updateUserProfile(username: String, bio: String, profileImageUrl: String?): User
    suspend fun getCurrentUser(): User?
    suspend fun logout()
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun verifyPhoneNumber(phoneNumber: String): String
    suspend fun verifyOtp(verificationId: String, otp: String): User
    suspend fun updateFcmToken(token: String)
    suspend fun signInWithCredential(credential: AuthCredential): String
} 