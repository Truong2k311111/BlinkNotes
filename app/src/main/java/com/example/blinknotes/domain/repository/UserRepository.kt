package com.example.blinknotes.domain.repository

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun getUserProfile(userId: String): User
    suspend fun getUserPosts(userId: String): List<Post>
    suspend fun updateUserProfile(username: String, bio: String, profileImageUrl: String?): User
    suspend fun followUser(targetUserId: String)
    suspend fun unfollowUser(targetUserId: String)
    suspend fun getFollowedUsers(): List<User>
    suspend fun getFollowers(userId: String): List<User>
    suspend fun getSuggestedUsers(): List<User>
    suspend fun blockUser(userId: String)
    suspend fun unblockUser(userId: String)
    suspend fun getBlockedUsers(): List<User>
    suspend fun updatePrivacySettings(isPrivate: Boolean)
    suspend fun updateSocialLinks(facebook: String?, instagram: String?, twitter: String?)
    suspend fun getLikedPosts(userId: String): List<Post>
    suspend fun getSavedPosts(userId: String): List<Post>
    suspend fun getDrafts(userId: String): List<Post>
    suspend fun searchUsers(query: String): List<User>
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByBlinkNotesId(blinkNotesId: String): User?
    suspend fun getAllUsers(): List<User>
    suspend fun getUsersByIds(userIds: List<String>): List<User>
} 