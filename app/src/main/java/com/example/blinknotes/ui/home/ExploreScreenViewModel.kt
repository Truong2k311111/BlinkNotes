package com.example.blinknotes.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.data.helper.FirestoreHelper.getAllPosts
import com.example.blinknotes.data.helper.FirestoreHelper.getAllPosts2
import com.example.blinknotes.data.helper.FirestoreHelper.getAllPostsExcludingUsers
import com.example.blinknotes.data.helper.FirestoreHelper.getUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FieldValue


data class Post(
    val id: String = "",
    val userId: String = "",
    val userIdCmt: String = "",
    val imageUrls: List<String> = emptyList(),
    val firstImageUrl: String = "",
    val caption: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val visibility: String = "public",
    val tags: List<String> = emptyList(),
    val status: String = "active",
)
data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profileImage: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val blinkNotesId: String = "",
    val bio: String = "",
    val coverImage: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val recentPost: List<RecentPost> = emptyList(),
    val hasStory: Boolean = false,
    val hasMoment: Boolean = false,
    val isOnline: Boolean = false,
    val isStoryActive: Boolean = false,
    val isMoment: Boolean = false,
    val isMomentSeen: Boolean = false,
    val isStorySeen: Boolean = false,
    val isFriend: Boolean = false,
    val note: String = "",
    val latestMessage: String = "",
    val unreadMessages: Int = 0,
    val fcmToken: String? = null,
    val facebookLink: String? = null,
    val instagramLink: String? = null,
    val twitterLink: String? = null,
    val blockedUsers : List<String> = emptyList(),
)

class ExploreScreenViewModel : ViewModel() {
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _users = MutableStateFlow<Map<String, User?>>(emptyMap())
    val users: StateFlow<Map<String, User?>> = _users
    private val _usersListBlock = MutableStateFlow<Map<String, User?>>(emptyMap())
    val usersListBlock: StateFlow<Map<String, User?>> = _usersListBlock

    private val _userCache = mutableMapOf<String, User?>()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _postLikeStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val postLikeStatus: StateFlow<Map<String, Boolean>> = _postLikeStatus

    private var lastVisiblePost: Post? = null
    private var _listUser = MutableStateFlow<List<String>>(emptyList())
    val listUser : StateFlow<List<String>> = _listUser
    internal var isLoading = false
    private val pageSize = 10
    private var loadedPostIds = mutableSetOf<String>()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    init {
        viewModelScope.launch {
            getAllUser()
            loadMorePosts()
        }
    }
    suspend  fun getAllUser() {
            try {
                FirebaseFirestore.getInstance().collection("users")
                    .get()
                    .addOnSuccessListener { documents ->
                        val users = documents.mapNotNull { it.toObject(User::class.java) }
                        val userId = documents.map { it.id }
                        val blockedByUsers = documents.filter { document ->
                            val blockedUsers = document.get("blockedUsers") as? List<String> ?: emptyList()
                            blockedUsers.contains(currentUserId)
                        }.map { it.id }
                        _listUser.value = blockedByUsers
                        Log.d("FirestoreUser", "Users loaded: ${_listUser.value}")
                        }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching users: ${e.message}")
            }
    }
    fun fetchUser(userId: String) {
        if (_userCache.containsKey(userId)) {
            _users.value = _users.value + (userId to _userCache[userId])
            return
        }
        viewModelScope.launch {
            getUser(userId) { fetchedUser ->
                _userCache[userId] = fetchedUser
                _users.value = _users.value + (userId to fetchedUser)
                if (fetchedUser != null) {
                    Log.d("FirestoreUser", "User loaded: $fetchedUser")
                } else {
                    Log.e("Firestore", "User not found")
                }
            }
        }
    }

    fun getPostByPostId(postId: String, callback: (Post?) -> Unit) {
        _posts.value.find { it.id == postId }?.let {
            callback(it)
            return
        }

        FirebaseFirestore.getInstance().collection("posts")
            .document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val post = document.toObject(Post::class.java)?.copy(id = document.id)
                    callback(post)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching post: ${e.message}")
                callback(null)
            }
    }

 suspend   fun loadMorePosts() {
        if (isLoading) return
        isLoading = true

            try {
                delay(800L) // Reduced delay for better UX
//            getAllPosts(listUser = _listUser.value, lastVisiblePost)
                getAllPosts2(lastVisiblePost)
               // getAllPostsExcludingUsers(listUser = _listUser.value,lastVisiblePost)
            { newPosts ->
                if (newPosts.isNotEmpty()) {
                        // Lọc ra các bài viết đã được tải trước đó
                        val uniqueNewPosts = newPosts.filter { post ->
                            !loadedPostIds.contains(post.id)
                        }

                        if (uniqueNewPosts.isNotEmpty()) {
                            lastVisiblePost = uniqueNewPosts.last()
                            val currentPosts = _posts.value
                            _posts.value = currentPosts + uniqueNewPosts
                            // Thêm ID của các bài viết mới vào set
                            loadedPostIds.addAll(uniqueNewPosts.map { it.id })
                        }
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error loading posts: ${e.message}")
                isLoading = false
            }
    }

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                lastVisiblePost = null
                _posts.value = emptyList()
                _userCache.clear()
                _users.value = emptyMap()
                loadedPostIds.clear() // Reset danh sách ID đã tải

                delay(1000L) // Add a small delay to show the refresh animation
                loadMorePosts()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7

        return when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            weeks < 4 -> "$weeks tuần trước"
            else -> {
                val sdf = SimpleDateFormat("dd 'tháng' MM", Locale("vi"))
                sdf.format(Date(timestamp))
            }
        }
    }
    fun checkPostLikeStatus(postId: String, userId: String) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("likes")
                    .document("${userId}_${postId}")
                    .get()
                    .addOnSuccessListener { document ->
                        _postLikeStatus.value = _postLikeStatus.value + (postId to document.exists())
                    }
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error checking like status", e)
            }
        }
    }
    fun togglePostLike(postId: String, userId: String) {
        viewModelScope.launch {
            try {
                val postRef = FirebaseFirestore.getInstance().collection("posts").document(postId)
                val likeRef = FirebaseFirestore.getInstance()
                    .collection("likes")
                    .document("${userId}_${postId}")

                // Kiểm tra trạng thái like hiện tại
                likeRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Nếu đã like thì unlike
                        likeRef.delete()
                        postRef.update("likesCount", FieldValue.increment(-1))
                            .addOnSuccessListener {
                                // Cập nhật số tim trong danh sách bài viết
                                val currentPosts = _posts.value
                                val updatedPosts = currentPosts.map { post ->
                                    if (post.id == postId) {
                                        post.copy(likesCount = (post.likesCount - 1).coerceAtLeast(0))
                                    } else {
                                        post
                                    }
                                }
                                _posts.value = updatedPosts
                                _postLikeStatus.value = _postLikeStatus.value + (postId to false)
                            }
                    } else {
                        // Nếu chưa like thì like
                        likeRef.set(mapOf(
                            "userId" to userId,
                            "postId" to postId,
                            "timestamp" to FieldValue.serverTimestamp()
                        ))
                        postRef.update("likesCount", FieldValue.increment(1))
                            .addOnSuccessListener {
                                // Cập nhật số tim trong danh sách bài viết
                                val currentPosts = _posts.value
                                val updatedPosts = currentPosts.map { post ->
                                    if (post.id == postId) {
                                        post.copy(likesCount = post.likesCount + 1)
                                    } else {
                                        post
                                    }
                                }
                                _posts.value = updatedPosts
                                _postLikeStatus.value = _postLikeStatus.value + (postId to true)
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error toggling like", e)
            }
        }
    }
}
