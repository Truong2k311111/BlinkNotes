package com.example.blinknotes.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

interface FirebaseAuthApi {
    val currentUser: FirebaseUser?
    val auth: FirebaseAuth

    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser
    suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser
    suspend fun signOut()
    suspend fun updateProfile(displayName: String, photoUrl: String? = null)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun updatePassword(newPassword: String)
    suspend fun deleteUser()
}

class FirebaseAuthApiImpl(
    override val auth: FirebaseAuth
) : FirebaseAuthApi {
    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser {
        return auth.signInWithEmailAndPassword(email, password).await().user!!
    }

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser {
        return auth.createUserWithEmailAndPassword(email, password).await().user!!
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun updateProfile(displayName: String, photoUrl: String?) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(photoUrl?.let { android.net.Uri.parse(it) })
            .build()
        currentUser?.updateProfile(profileUpdates)?.await()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun updatePassword(newPassword: String) {
        currentUser?.updatePassword(newPassword)?.await()
    }

    override suspend fun deleteUser() {
        currentUser?.delete()?.await()
    }
} 