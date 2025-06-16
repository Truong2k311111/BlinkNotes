package com.example.blinknotes.data.repository

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid ?: throw Exception("Login failed")
        
        // Check if user is blocked
        val userDoc = firestore.collection("users").document(userId).get().await()
        val isBlocked = userDoc.getBoolean("isBlocked") ?: false
        
        if (isBlocked) {
            // Sign out the user if they are blocked
            auth.signOut()
            throw Exception("Tài khoản của bạn đã bị khóa")
        }
        
        return getUserFromFirestore(userId)
    }

    override suspend fun register(email: String, password: String, username: String): User {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid ?: throw Exception("Registration failed")
        
        val user = User(
            userId = userId,
            username = username,
            email = email,
            profileImage = "",
            followers = emptyList(),
            following = emptyList(),
            createdAt = System.currentTimeMillis(),
            blinkNotesId = "",
            bio = "",
            coverImage = "",
            followersCount = 0,
            followingCount = 0,
            recentPost = emptyList()
        )
        
        firestore.collection("users").document(userId).set(user).await()
        return user
    }

    override suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun updateUserProfile(username: String, bio: String, profileImageUrl: String?): User {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val updates = mutableMapOf<String, Any>()
        
        if (username.isNotBlank()) updates["username"] = username
        if (bio.isNotBlank()) updates["bio"] = bio
        if (profileImageUrl != null) updates["profileImage"] = profileImageUrl
        
        firestore.collection("users").document(userId).update(updates).await()
        return getUserFromFirestore(userId)
    }

    override suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return getUserFromFirestore(userId)
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun verifyPhoneNumber(phoneNumber: String): String {
        // Implementation for phone verification
        return ""
    }

    override suspend fun verifyOtp(verificationId: String, otp: String): User {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        val result = auth.signInWithCredential(credential).await()
        val userId = result.user?.uid ?: throw Exception("OTP verification failed")
        return getUserFromFirestore(userId)
    }

    override suspend fun updateFcmToken(token: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("fcmToken", token)
            .await()
    }

    override suspend fun signInWithCredential(credential: AuthCredential): String {
        val result = auth.signInWithCredential(credential).await()
        return result.user?.uid ?: throw Exception("Google Sign-In failed: User UID is null")
    }

    private suspend fun getUserFromFirestore(userId: String): User {
        val doc = firestore.collection("users").document(userId).get().await()
        return doc.toObject(User::class.java) ?: throw Exception("User not found")
    }
} 