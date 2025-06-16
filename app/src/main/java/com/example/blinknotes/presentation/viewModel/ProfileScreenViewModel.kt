package com.example.blinknotes.presentation.viewModel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.data.helper.FirestoreHelper.getUser
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.model.PostWithUser
import com.example.blinknotes.domain.model.RecentPost
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.usecase.auth.UpdateUserProfileUseCase
import com.example.blinknotes.domain.usecase.user.FollowUserUseCase
import com.example.blinknotes.domain.usecase.user.GetUserPostsUseCase
import com.example.blinknotes.domain.usecase.user.GetUserProfileUseCase
import com.example.blinknotes.domain.usecase.user.UnfollowUserUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

sealed class ProfileScreenUiState {
    object Initial : ProfileScreenUiState()
    object Loading : ProfileScreenUiState()
    data class Success(
        val user: User,
        val posts: List<Post>,
        val isFollowing: Boolean
    ) : ProfileScreenUiState()
    data class Error(val error: ProfileScreenError) : ProfileScreenUiState()
}

sealed class ProfileScreenError {
    data class NetworkError(val message: String) : ProfileScreenError()
    data class DatabaseError(val message: String) : ProfileScreenError()
    data class AuthError(val message: String) : ProfileScreenError()
    data class ValidationError(val message: String) : ProfileScreenError()
}

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val loggedInUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _postsWithUsers = mutableStateListOf<PostWithUser>()
    val postsWithUsers: List<PostWithUser> get() = _postsWithUsers

    private val loadedUsers = mutableMapOf<String, User>()

    private val _drafts = mutableStateListOf<Post>()
    val drafts: List<Post> get() = _drafts

    private val _followers = mutableStateListOf<User>()
    val followers: List<User> get() = _followers

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    private val _userAll = MutableStateFlow<List<User>>(emptyList())
    val userAll: StateFlow<List<User>> = _userAll

    private val _isPrivateAccount = MutableStateFlow(false)
    val isPrivateAccount: StateFlow<Boolean> = _isPrivateAccount

    private val _blockedUsers = MutableStateFlow<List<User>>(emptyList())
    val blockedUsers: StateFlow<List<User>> = _blockedUsers

    private val _savedPostsWithUsers = mutableStateListOf<PostWithUser>()
    val savedPostsWithUsers: List<PostWithUser> get() = _savedPostsWithUsers

    private val _uiState = MutableStateFlow<ProfileScreenUiState>(ProfileScreenUiState.Initial)
    val uiState: StateFlow<ProfileScreenUiState> = _uiState.asStateFlow()

    init {
        loadPrivacySettings()
        fetchAllUser()
    }

    fun fetchAllUser() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { document ->
                    document.toObject(User::class.java)?.copy(userId = document.id)
                }
                _userAll.value = users
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreenViewModel", "Error fetching all users: ${e.message}")
            }
    }

    fun blockUser(userId: String, onSuccess: @Composable () -> Unit = {}, onFailure: @Composable (Exception) -> Unit = {}) {
        loggedInUserId?.let { currentUserId ->
            db.collection("users").document(currentUserId)
                .update("blockedUsers", FieldValue.arrayUnion(userId))
                .addOnSuccessListener {
                    _blockedUsers.value = _blockedUsers.value + User(userId = userId)
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreenViewModel", "Error blocking user: ${e.message}")
                }
        }
    }

    private fun loadPrivacySettings() {
        loggedInUserId?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    _isPrivateAccount.value = document.getBoolean("isPrivate") ?: false
                    val blockedUserIds = document.get("blockedUsers") as? List<String> ?: emptyList()
                    loadBlockedUsers(blockedUserIds)
                }
        }
    }

    private fun loadBlockedUsers(userIds: List<String>) {
        if (userIds.isEmpty()) {
            _blockedUsers.value = emptyList()
            return
        }

        db.collection("users").whereIn(FieldPath.documentId(), userIds).get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { document ->
                    document.toObject(User::class.java)?.copy(userId = document.id)
                }

                _blockedUsers.value = users
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreenViewModel", "Error loading blocked users: ${e.message}")
            }
    }

    fun setPrivateAccount(isPrivate: Boolean) {
        loggedInUserId?.let { userId ->
            db.collection("users").document(userId).update("isPrivate", isPrivate)
                .addOnSuccessListener { _isPrivateAccount.value = isPrivate }
        }
    }

    fun unblockUser(userId: String) {
        try {
            val userToUnblock = blockedUsers.value.find { it.userId == userId }
            if (userToUnblock != null) {
                // Remove the user from the blocked list in the database
                db.collection("users")
                    .document(loggedInUserId ?: "")
                    .update("blockedUsers", FieldValue.arrayRemove(userId))
                    .addOnSuccessListener {
                        _blockedUsers.value = _blockedUsers.value.filter { it.userId != userId }
                    }
                    .addOnFailureListener { e ->
                    }
        } else {
                Log.e("ProfileScreenViewModel", "User with ID $userId not found in blocked users list")
            }
            } catch (e: Exception) {
            Log.e("ProfileScreenViewModel", "Error in unblockUser: ${e.message}")
        }
    }

    fun fetchUser(userId: String) {
        if (userId.isBlank()) {
            Log.e("ProfileScreenViewModel", "Invalid userId: $userId")
            return
        }

        val userRef = db.collection("users").document(userId)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    // Handle the fetched user
                } else {
                    Log.e("ProfileScreenViewModel", "User not found for ID: $userId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreenViewModel", "Error fetching user: ${e.message}")
            }
    }

    fun updateSocialLinks(userId: String, facebook: String, instagram: String, twitter: String) {
        val updates = mutableMapOf<String, Any>()
        if (facebook.isNotEmpty()) updates["facebookLink"] = facebook
        if (instagram.isNotEmpty()) updates["instagramLink"] = instagram
        if (twitter.isNotEmpty()) updates["twitterLink"] = twitter

        if (updates.isNotEmpty()) {
            db.collection("users").document(userId).update(updates)
                .addOnSuccessListener {
                    fetchUser(userId)
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreenViewModel", "Error updating social links: ${e.message}")
                }
        }
    }

    fun getCurrentUser(userId: String, callback: (User?) -> Unit) {
        if (userId.isNotEmpty()) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data
                        if (data != null) {
                            val user = User(
                                userId = document.id,
                                username = data["username"] as? String ?: "",
                                email = data["email"] as? String ?: "",
                                profileImage = data["profileImage"] as? String ?: "",
                                coverImage = data["coverImage"] as? String ?: "",
                                blinkNotesId = data["blinkNotesId"] as? String ?: "",
                                bio = data["bio"] as? String ?: "",
                                followers = data["followers"] as? List<String> ?: emptyList(),
                                following = data["following"] as? List<String> ?: emptyList(),
                                createdAt = data["createdAt"] as? Long
                                    ?: System.currentTimeMillis(),
                                followersCount = (data["followersCount"] as? Long)?.toInt() ?: 0,
                                followingCount = (data["followingCount"] as? Long)?.toInt() ?: 0,
                                recentPost = data["recentPost"] as? List<RecentPost> ?: emptyList()
                            )
                            Log.d("ProfileScreen", "Cover image URL: ${user.coverImage}")
                            callback(user)
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                }
        }
    }

    fun updateProfile(userId: String, username: String, blinkNotesId: String, bio: String) {
        db.collection("users")
            .document(userId)
            .update(
                mapOf(
                    "username" to username,
                    "blinkNotesId" to blinkNotesId,
                    "bio" to bio
                )
            )
            .addOnSuccessListener {
                Log.d("ProfileScreen", "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error updating profile: ${e.message}")
            }
    }

    fun updateCoverImage(userId: String, coverImageUrl: String, onSuccess: () -> Unit = {}) {
        db.collection("users")
            .document(userId)
            .update("coverImage", coverImageUrl)
            .addOnSuccessListener {
                Log.d("ProfileScreen", "Cover image updated successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error updating cover image: ${e.message}")
            }
    }

    fun getUserPosts(userId: String, lastPost: Post? = null, callback: (List<Post>) -> Unit) {
        FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("userId", userId)
            .whereNotEqualTo("status", "draft")
            .get()
            .addOnSuccessListener { result ->
                val postsList = result.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val userIdCmt = doc.getString("userIdCmt") ?: ""
                        val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                        val firstImageUrl = imageUrls.firstOrNull() ?: ""
                        val caption = doc.getString("caption") ?: ""
                        val content = doc.getString("content") ?: ""
                        val createdAt = doc.getLong("createdAt") ?: 0L
                        val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                        val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                        val visibility = doc.getString("visibility") ?: "public"
                        val tags = doc.get("tags") as? List<String> ?: emptyList()

                        Post(
                            id,
                            userId,
                            userIdCmt,
                            imageUrls,
                            firstImageUrl,
                            caption,
                            content,
                            createdAt,
                            likesCount,
                            commentsCount,
                            visibility,
                            tags
                        )
                    } catch (e: Exception) {
                        Log.e("ProfileScreenViewModel", "Error parsing post ${doc.id}: ${e.message}", e)
                        null
                    }
                }
                val shuffledPosts = postsList.shuffled()
                val filteredPosts = if (lastPost != null) {
                    shuffledPosts.filter { it.id != lastPost.id }
                } else {
                    shuffledPosts
                }
                callback(filteredPosts.take(10))
            }
            .addOnFailureListener { e ->
                callback(emptyList())
                Log.e("Firestore", "Lỗi khi tải dữ liệu: ${e.message}")
            }
    }

    fun getUserLikedPostsWithUser(
        userId: String,
        callback: (List<PostWithUser>) -> Unit
    ) {
        db.collection("likes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { likesSnapshot ->
                val likedPostIds = likesSnapshot.documents.mapNotNull { it.getString("postId") }
                    .filter { it.isNotBlank() }

                if (likedPostIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }
                db.collection("posts")
                    .whereIn(FieldPath.documentId(), likedPostIds)
                    .get()
                    .addOnSuccessListener { postsSnapshot ->
                        val postsList = mutableListOf<Post>()
                        val userIdsSet = mutableSetOf<String>()
                        for (doc in postsSnapshot.documents) {
                            try {
                                val id = doc.id
                                val userId = doc.getString("userId") ?: ""
                                val userIdCmt = doc.getString("userIdCmt") ?: ""
                                val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                                val firstImageUrl = imageUrls.firstOrNull() ?: ""
                                val caption = doc.getString("caption") ?: ""
                                val content = doc.getString("content") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: 0L
                                val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                                val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                                val visibility = doc.getString("visibility") ?: "public"
                                val tags = doc.get("tags") as? List<String> ?: emptyList()
                                postsList.add(
                                    Post(
                                        id,
                                        userId,
                                        userIdCmt,
                                        imageUrls,
                                        firstImageUrl,
                                        caption,
                                        content,
                                        createdAt,
                                        likesCount,
                                        commentsCount,
                                        visibility,
                                        tags
                                    )
                                )
                                if (userId.isNotBlank()) userIdsSet.add(userId)
                                if (userIdCmt.isNotBlank()) userIdsSet.add(userIdCmt)
                            } catch (e: Exception) {
                                Log.e("PostLoad", "Error parsing post: ${e.message}")
                            }
                        }
                        if (userIdsSet.isEmpty()) {
                            val result = postsList.map { PostWithUser(it, null) }
                            callback(result)
                            return@addOnSuccessListener
                        }

                        val userMap = mutableMapOf<String, User?>()
                        val userFetchCount = userIdsSet.size
                        var usersFetched = 0
                        userIdsSet.forEach { id ->
                            getUser(id) { user ->
                                userMap[id] = user
                                usersFetched++
                                if (usersFetched == userFetchCount) {
                                    val result = postsList.map { post ->
                                        val user = userMap[post.userId] ?: userMap[post.userIdCmt]
                                        PostWithUser(post, user)
                                    }
                                    callback(result)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("PostLoad", "Error getting posts: ${e.message}")
                        callback(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PostLoad", "Error getting likes: ${e.message}")
                callback(emptyList())
            }
    }

    fun loadLikedPosts(userId: String) {
        getUserLikedPostsWithUser(userId) { list ->
            Log.d("LoadLikedPosts", "Số lượng post load được: ${list.size}")

            list.forEachIndexed { index, postWithUser ->
                val username = postWithUser.user?.username ?: "Không có user"
                val postId = postWithUser.post.id
                Log.d("LoadLikedPosts", "[$index] Post ID: $postId - User: $username")
            }

            _postsWithUsers.clear()
            _postsWithUsers.addAll(list)
        }
    }

    fun loadDrafts(userId: String) {
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "draft")
            .get()
            .addOnSuccessListener { result ->
                val draftList = result.documents.mapNotNull { doc ->
                    try {
                        val draftId = doc.id
                        val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                        val firstImageUrl = imageUrls.firstOrNull() ?: ""
                        val caption = doc.getString("caption") ?: ""
                        val content = doc.getString("content") ?: ""
                        val createdAt = doc.getLong("createdAt") ?: 0L
                        val visibility = doc.getString("visibility") ?: "public"

                        Post(
                            id = draftId,
                            userId = userId,
                            userIdCmt = "",
                            imageUrls = imageUrls,
                            firstImageUrl = firstImageUrl,
                            caption = caption,
                            content = content,
                            createdAt = createdAt,
                            likesCount = 0,
                            commentsCount = 0,
                            visibility = visibility,
                            tags = emptyList()
                        )
                    } catch (e: Exception) {
                        Log.e("ProfileScreenViewModel", "Error parsing draft: ${e.message}")
                        null
                    }
                }
                _drafts.clear()
                _drafts.addAll(draftList)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreenViewModel", "Error loading drafts: ${e.message}")
            }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit) {
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d("ProfileScreenViewModel", "Post deleted successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreenViewModel", "Error deleting post: ${e.message}")
            }
    }

    fun loadSavedPosts(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("saves")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { savedSnapshot ->
                val savedPostIds = savedSnapshot.documents.mapNotNull { it.getString("postId") }
                if (savedPostIds.isEmpty()) {
                    _savedPostsWithUsers.clear()
                    return@addOnSuccessListener
                }
                db.collection("posts")
                    .whereIn(FieldPath.documentId(), savedPostIds)
                    .get()
                    .addOnSuccessListener { postsSnapshot ->
                        val postsList = mutableListOf<Post>()
                        val userIdsSet = mutableSetOf<String>()
                        for (doc in postsSnapshot.documents) {
                            try {
                                val id = doc.id
                                val userId = doc.getString("userId") ?: ""
                                val userIdCmt = doc.getString("userIdCmt") ?: ""
                                val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                                val firstImageUrl = imageUrls.firstOrNull() ?: ""
                                val caption = doc.getString("caption") ?: ""
                                val content = doc.getString("content") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: 0L
                                val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                                val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                                val visibility = doc.getString("visibility") ?: "public"
                                val tags = doc.get("tags") as? List<String> ?: emptyList()
                                postsList.add(
                                    Post(
                                        id,
                                        userId,
                                        userIdCmt,
                                        imageUrls,
                                        firstImageUrl,
                                        caption,
                                        content,
                                        createdAt,
                                        likesCount,
                                        commentsCount,
                                        visibility,
                                        tags
                                    )
                                )
                                if (userId.isNotBlank()) userIdsSet.add(userId)
                                if (userIdCmt.isNotBlank()) userIdsSet.add(userIdCmt)
                            } catch (e: Exception) {
                                Log.e("SavedPostLoad", "Error parsing post: ${e.message}")
                            }
                        }
                        if (userIdsSet.isEmpty()) {
                            val result = postsList.map { PostWithUser(it, null) }
                            _savedPostsWithUsers.clear()
                            _savedPostsWithUsers.addAll(result)
                            return@addOnSuccessListener
                        }
                        val userMap = mutableMapOf<String, User?>()
                        val userFetchCount = userIdsSet.size
                        var usersFetched = 0
                        userIdsSet.forEach { id ->
                            getUser(id) { user ->
                                userMap[id] = user
                                usersFetched++
                                if (usersFetched == userFetchCount) {
                                    val result = postsList.map { post ->
                                        val user = userMap[post.userId] ?: userMap[post.userIdCmt]
                                        PostWithUser(post, user)
                                    }
                                    _savedPostsWithUsers.clear()
                                    _savedPostsWithUsers.addAll(result)
                                }
                            }
                        }
                    }
            }
    }

    fun deleteSavedPost(postId: String, onSuccess: () -> Unit) {
        val userId = loggedInUserId ?: return
        db.collection("saves")
            .whereEqualTo("userId", userId)
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                result.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    _savedPostsWithUsers.removeAll { it.post.id == postId }
                    onSuccess()
                }
            }
    }

    fun deleteLikedPost(postId: String, onSuccess: () -> Unit) {
        val userId = loggedInUserId ?: return
        db.collection("likes")
            .whereEqualTo("userId", userId)
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                result.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    _postsWithUsers.removeAll { it.post.id == postId }
                    onSuccess()
                }
            }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileScreenUiState.Loading
                val user = getUserProfileUseCase(userId)
                val posts = getUserPostsUseCase(userId)
                val isFollowing = user.followers.contains(getCurrentUserId())
                _uiState.value = ProfileScreenUiState.Success(user, posts, isFollowing)
            } catch (e: Exception) {
                _uiState.value = ProfileScreenUiState.Error(
                    when (e) {
                        is FirebaseFirestoreException -> ProfileScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ProfileScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun updateProfile(username: String, bio: String, profileImageUrl: String?) {
        if (username.isBlank()) {
            _uiState.value = ProfileScreenUiState.Error(ProfileScreenError.ValidationError("Username cannot be empty"))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = ProfileScreenUiState.Loading
                updateUserProfileUseCase(username, bio, profileImageUrl)
                loadUserProfile(getCurrentUserId())
            } catch (e: Exception) {
                _uiState.value = ProfileScreenUiState.Error(
                    when (e) {
                        is FirebaseFirestoreException -> ProfileScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ProfileScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileScreenUiState.Loading
                followUserUseCase(targetUserId)
                loadUserProfile(targetUserId)
            } catch (e: Exception) {
                _uiState.value = ProfileScreenUiState.Error(
                    when (e) {
                        is FirebaseFirestoreException -> ProfileScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ProfileScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileScreenUiState.Loading
                unfollowUserUseCase(targetUserId)
                loadUserProfile(targetUserId)
            } catch (e: Exception) {
                _uiState.value = ProfileScreenUiState.Error(
                    when (e) {
                        is FirebaseFirestoreException -> ProfileScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ProfileScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    private fun getCurrentUserId(): String {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    fun loadFollowedUsers() {
        loggedInUserId?.let { userId ->
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val following = document.get("following") as? List<String> ?: emptyList()
                    if (following.isNotEmpty()) {
                        db.collection("users")
                            .whereIn(FieldPath.documentId(), following)
                            .get()
                            .addOnSuccessListener { result ->
                                val users = result.documents.mapNotNull { doc ->
                                    doc.toObject(User::class.java)?.copy(userId = doc.id)
                                }
                                _followers.clear()
                                _followers.addAll(users)
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProfileScreenViewModel", "Error loading followed users: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreenViewModel", "Error getting following list: ${e.message}")
                }
        }
    }

    fun updateUser(user: User) {
        _user.value = user
    }
}
