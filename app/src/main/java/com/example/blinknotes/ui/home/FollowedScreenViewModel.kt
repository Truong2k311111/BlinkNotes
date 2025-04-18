package com.example.blinknotes.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class FollowedScreenViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _followedUsers = MutableStateFlow<List<User>>(emptyList())
    val followedUsers: StateFlow<List<User>> = _followedUsers
    
    private val _suggestedUsers = MutableStateFlow<List<User>>(emptyList())
    val suggestedUsers: StateFlow<List<User>> = _suggestedUsers
    
    private val _userPosts = MutableStateFlow<Map<String, List<Post>>>(emptyMap())
    val userPosts: StateFlow<Map<String, List<Post>>> = _userPosts
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasFollowedUsers = MutableStateFlow(false)
    val hasFollowedUsers: StateFlow<Boolean> = _hasFollowedUsers

    private val _userIdListFollowed = MutableStateFlow<List<String>>(emptyList())
    val userIdListFollowed: StateFlow<List<String>> = _userIdListFollowed
    
    init {
        loadFollowedUsers()
        loadSuggestedUsers()
    }
    
    private suspend fun getRecentPosts(userId: String, limit: Int): List<RecentPost> {
        return try {
            val posts = db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            posts.documents.mapNotNull { doc ->
                val imageUrls = doc.get("imageUrls") as? List<String>
                val firstImageUrl = imageUrls?.firstOrNull() ?: return@mapNotNull null
                
                RecentPost(
                    id = doc.id,
                    imageUrl = firstImageUrl,
                    caption = doc.getString("caption") ?: "",
                    timestamp = Date(doc.getLong("createdAt") ?: System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            Log.e("FollowedViewModel", "Error getting recent posts", e)
            emptyList()
        }
    }
    
    fun loadFollowedUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Get current user's following list
                val currentUserDoc = db.collection("users").document(currentUserId).get().await()
                val followingList = currentUserDoc.get("following") as? List<String> ?: emptyList()
                
                if (followingList.isEmpty()) {
                    _followedUsers.value = emptyList()
                    _hasFollowedUsers.value = false
                    _isLoading.value = false
                    return@launch
                }
                
                _hasFollowedUsers.value = true
                
                // Get user details for each followed user
                val users = mutableListOf<User>()
                
                for (userId in followingList) {
                    val userDoc = db.collection("users").document(userId).get().await()
                    if (userDoc.exists()) {
                        val followers = userDoc.get("followers") as? List<String> ?: emptyList()
                        val following = userDoc.get("following") as? List<String> ?: emptyList()
                        
                        // Get recent posts for this user
                        val recentPosts = getRecentPosts(userId, 2)
                        
                        val user = User(
                            userId = userId,
                            username = userDoc.getString("username") ?: "",
                            email = userDoc.getString("email") ?: "",
                            profileImage = userDoc.getString("profileImage") ?: "",
                            followers = followers,
                            following = following,
                            createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                            blinkNotesId = userDoc.getString("blinkNotesId") ?: "",
                            bio = userDoc.getString("bio") ?: "",
                            coverImage = userDoc.getString("coverImage") ?: "",
                            followersCount = followers.size,
                            followingCount = following.size,
                            recentPost = recentPosts
                        )
                        
                        users.add(user)
                    }
                }
                
                _followedUsers.value = users
                _userIdListFollowed.value = users.map { it.userId }
            } catch (e: Exception) {
                Log.e("FollowedViewModel", "Error loading followed users", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadSuggestedUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                // Get current user's following list
                val currentUserDoc = db.collection("users").document(currentUserId).get().await()
                val followingList = currentUserDoc.get("following") as? List<String> ?: emptyList()
                
                // Get all users except current user and already followed users
                val querySnapshot = db.collection("users")
                    .whereNotIn("userId", listOf(currentUserId) + followingList)
                    .limit(10)
                    .get()
                    .await()
                
                val users = mutableListOf<User>()
                
                for (doc in querySnapshot.documents) {
                    val userId = doc.id
                    val followers = doc.get("followers") as? List<String> ?: emptyList()
                    val following = doc.get("following") as? List<String> ?: emptyList()
                    
                    val user = User(
                        userId = userId,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        profileImage = doc.getString("profileImage") ?: "",
                        followers = followers,
                        following = following,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        blinkNotesId = doc.getString("blinkNotesId") ?: "",
                        bio = doc.getString("bio") ?: "",
                        coverImage = doc.getString("coverImage") ?: "",
                        followersCount = followers.size,
                        followingCount = following.size,
                        recentPost = emptyList() // We don't need recent posts for suggested users
                    )
                    
                    users.add(user)
                }
                
                _suggestedUsers.value = users
            } catch (e: Exception) {
                Log.e("FollowedViewModel", "Error loading suggested users", e)
            }
        }
    }
    
    fun followUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                // Add to current user's following list
                db.collection("users").document(currentUserId)
                    .update("following", FieldValue.arrayUnion(targetUserId))
                
                // Add current user to target user's followers list
                db.collection("users").document(targetUserId)
                    .update("followers", FieldValue.arrayUnion(currentUserId))
                
                // Refresh lists
                loadFollowedUsers()
                loadSuggestedUsers()
            } catch (e: Exception) {
                Log.e("FollowedViewModel", "Error following user", e)
            }
        }
    }
    
    fun unfollowUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                // Remove from current user's following list
                db.collection("users").document(currentUserId)
                    .update("following", FieldValue.arrayRemove(targetUserId))
                
                // Remove current user from target user's followers list
                db.collection("users").document(targetUserId)
                    .update("followers", FieldValue.arrayRemove(currentUserId))
                
                // Refresh lists
                loadFollowedUsers()
                loadSuggestedUsers()
            } catch (e: Exception) {
                Log.e("FollowedViewModel", "Error unfollowing user", e)
            }
        }
    }
    
    fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            try {
                val postsSnapshot = db.collection("posts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val postsList = mutableListOf<Post>()
                for (doc in postsSnapshot) {
                    val postData = doc.data
                    val postId = doc.id
                    val imageUrls = postData["imageUrls"] as? List<String> ?: emptyList()
                    val caption = postData["caption"] as? String ?: ""
                    val content = postData["content"] as? String ?: ""
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    val likesCount = (postData["likes"] as? List<String> ?: emptyList()).size
                    
                    val post = Post(
                        id = postId,
                        userId = userId,
                        imageUrls = imageUrls,
                        firstImageUrl = imageUrls.firstOrNull() ?: "",
                        caption = caption,
                        content = content,
                        createdAt = createdAt,
                        likesCount = likesCount
                    )
                    
                    postsList.add(post)
                }
                
                // Cập nhật danh sách bài đăng của người dùng
                val currentPosts = _userPosts.value.toMutableMap()
                currentPosts[userId] = postsList
                _userPosts.value = currentPosts
            } catch (e: Exception) {
                Log.e("FollowedScreenViewModel", "Error loading user posts", e)
            }
        }
    }
    
    fun isFollowing(targetUserId: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false
        return _followedUsers.value.any { it.userId == targetUserId }
    }
}

data class RecentPost(
    val id: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: Date
) 