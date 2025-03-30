package com.example.blinknotes.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.data.helper.FirestoreHelper
import com.example.blinknotes.data.helper.FirestoreHelper.getAllPosts
import com.example.blinknotes.data.helper.FirestoreHelper.getUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
    val tags: List<String> = emptyList()
)
data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profileImage: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

class ExploreScreenViewModel : ViewModel() {
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _users = MutableStateFlow<Map<String, User?>>(emptyMap())
    val users: StateFlow<Map<String, User?>> = _users

    private val _userCache = mutableMapOf<String, User?>()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private var lastVisiblePost: Post? = null
    internal var isLoading = false
    private val pageSize = 10
    private var loadedPostIds = mutableSetOf<String>()

    init {
        loadMorePosts()
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

    fun loadMorePosts() {
        if (isLoading) return
        isLoading = true
        
        viewModelScope.launch {
            try {
                delay(800L) // Reduced delay for better UX
                getAllPosts(lastVisiblePost) { newPosts ->
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
//
//    fun checkPostLikeStatus(postId: String, userId: String, callback: (Boolean) -> Unit) {
//        FirebaseFirestore.getInstance().collection("likes")
//            .whereEqualTo("postId", postId)
//            .whereEqualTo("userId", userId)
//            .get()
//            .addOnSuccessListener { result ->
//                callback(!result.isEmpty)
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Error checking like status: ${e.message}")
//                callback(false)
//            }
//    }
//
//    fun togglePostLike(postId: String, userId: String) {
//        FirebaseFirestore.getInstance().collection("likes")
//            .whereEqualTo("postId", postId)
//            .whereEqualTo("userId", userId)
//            .get()
//            .addOnSuccessListener { result ->
//                if (result.isEmpty) {
//                    // Nếu chưa like, thêm like mới
//                    FirebaseFirestore.getInstance().collection("likes")
//                        .add(mapOf(
//                            "postId" to postId,
//                            "userId" to userId,
//                            "createdAt" to System.currentTimeMillis()
//                        ))
//                        .addOnSuccessListener {
//                            // Cập nhật UI
//                            _posts.value = _posts.value.map { post ->
//                                if (post.id == postId) {
//                                    post.copy(likes = post.likes + userId)
//                                } else {
//                                    post
//                                }
//                            }
//                        }
//                } else {
//                    // Nếu đã like, xóa like
//                    result.documents.forEach { doc ->
//                        FirebaseFirestore.getInstance().collection("likes")
//                            .document(doc.id)
//                            .delete()
//                            .addOnSuccessListener {
//                                // Cập nhật UI
//                                _posts.value = _posts.value.map { post ->
//                                    if (post.id == postId) {
//                                        post.copy(likes = post.likes - userId)
//                                    } else {
//                                        post
//                                    }
//                                }
//                            }
//                    }
//                }
//            }
//    }
}
