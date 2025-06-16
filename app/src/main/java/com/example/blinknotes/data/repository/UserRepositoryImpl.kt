package com.example.blinknotes.data.repository

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUserProfile(userId: String): User {
        val doc = firestore.collection("users").document(userId).get().await()
        return doc.toObject(User::class.java) ?: throw Exception("User not found")
    }

    override suspend fun getUserPosts(userId: String): List<Post> {
        val snapshot = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .whereNotEqualTo("status", "draft")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun updateUserProfile(username: String, bio: String, profileImageUrl: String?): User {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val updates = mutableMapOf<String, Any>()
        
        if (username.isNotBlank()) updates["username"] = username
        if (bio.isNotBlank()) updates["bio"] = bio
        if (profileImageUrl != null) updates["profileImage"] = profileImageUrl
        
        firestore.collection("users").document(userId).update(updates).await()
        return getUserProfile(userId)
    }

    override suspend fun followUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        
        firestore.collection("users").document(currentUserId)
            .update("following", FieldValue.arrayUnion(targetUserId))
            .await()
            
        firestore.collection("users").document(targetUserId)
            .update("followers", FieldValue.arrayUnion(currentUserId))
            .await()
    }

    override suspend fun unfollowUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        
        firestore.collection("users").document(currentUserId)
            .update("following", FieldValue.arrayRemove(targetUserId))
            .await()
            
        firestore.collection("users").document(targetUserId)
            .update("followers", FieldValue.arrayRemove(currentUserId))
            .await()
    }

    override suspend fun getFollowedUsers(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val currentUser = getUserProfile(currentUserId)
        
        return currentUser.following.mapNotNull { userId ->
            try {
                getUserProfile(userId)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getFollowers(userId: String): List<User> {
        val user = getUserProfile(userId)
        return user.followers.mapNotNull { followerId ->
            try {
                getUserProfile(followerId)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getSuggestedUsers(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val currentUser = getUserProfile(currentUserId)
        
        val snapshot = firestore.collection("users")
            .whereNotIn("userId", listOf(currentUserId) + currentUser.following)
            .limit(10)
            .get()
            .await()
            
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(userId = doc.id)
        }
    }

    override suspend fun blockUser(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        firestore.collection("users").document(currentUserId)
            .update("blockedUsers", FieldValue.arrayUnion(userId))
            .await()
    }

    override suspend fun unblockUser(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        firestore.collection("users").document(currentUserId)
            .update("blockedUsers", FieldValue.arrayRemove(userId))
            .await()
    }

    override suspend fun getBlockedUsers(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val currentUser = getUserProfile(currentUserId)
        
        return currentUser.blockedUsers.mapNotNull { userId ->
            try {
                getUserProfile(userId)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun updatePrivacySettings(isPrivate: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        firestore.collection("users").document(currentUserId)
            .update("isPrivate", isPrivate)
            .await()
    }

    override suspend fun updateSocialLinks(facebook: String?, instagram: String?, twitter: String?) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val updates = mutableMapOf<String, Any>()
        
        facebook?.let { updates["facebookLink"] = it }
        instagram?.let { updates["instagramLink"] = it }
        twitter?.let { updates["twitterLink"] = it }
        
        if (updates.isNotEmpty()) {
            firestore.collection("users").document(currentUserId)
                .update(updates)
                .await()
        }
    }

    override suspend fun getLikedPosts(userId: String): List<Post> {
        val likesSnapshot = firestore.collection("likes")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            
        val postIds = likesSnapshot.documents.mapNotNull { it.getString("postId") }
        
        if (postIds.isEmpty()) return emptyList()
        
        val postsSnapshot = firestore.collection("posts")
            .whereIn("id", postIds)
            .get()
            .await()
            
        return postsSnapshot.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getSavedPosts(userId: String): List<Post> {
        val savesSnapshot = firestore.collection("saves")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            
        val postIds = savesSnapshot.documents.mapNotNull { it.getString("postId") }
        
        if (postIds.isEmpty()) return emptyList()
        
        val postsSnapshot = firestore.collection("posts")
            .whereIn("id", postIds)
            .get()
            .await()
            
        return postsSnapshot.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getDrafts(userId: String): List<Post> {
        val snapshot = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "draft")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun searchUsers(query: String): List<User> {
        val snapshot = firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + '\uf8ff')
            .limit(20)
            .get()
            .await()
            
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(userId = doc.id)
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.copy(userId = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserByBlinkNotesId(blinkNotesId: String): User? {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("blinkNotesId", blinkNotesId)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(User::class.java)?.copy(userId = doc.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100) // Limit to prevent loading too much data
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(userId = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        
        return try {
            val snapshot = firestore.collection("users")
                .whereIn("userId", userIds)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(userId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "Error getting users by IDs: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.d("UserRepositoryImpl", "No current user found")
                return null
            }

            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            if (!userDoc.exists()) {
                Log.d("UserRepositoryImpl", "User document not found for ID: ${currentUser.uid}")
                return null
            }

            userDoc.toObject(User::class.java)?.copy(userId = userDoc.id)
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "Error getting current user: ${e.message}")
            null
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userData = hashMapOf<String, Any>(
                "userId" to user.userId,
                "username" to user.username,
                "email" to user.email,
                "profileImage" to user.profileImage,
                "bio" to user.bio,
                "following" to user.following,
                "followers" to user.followers,
                "isPrivate" to user.isOnline,
                "facebookLink" to (user.facebookLink ?: ""),
                "instagramLink" to (user.instagramLink ?: ""),
                "blinkNotesId" to user.blinkNotesId,
                "coverImage" to user.coverImage,
                "followersCount" to user.followersCount,
                "followingCount" to user.followingCount,
                "recentPost" to user.recentPost,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(user.userId).update(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "Error updating user: ${e.message}")
            Result.failure(e)
        }
    }
} 