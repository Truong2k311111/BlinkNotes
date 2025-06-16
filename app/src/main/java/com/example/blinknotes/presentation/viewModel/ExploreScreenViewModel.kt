package com.example.blinknotes.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.usecase.post.GetPostsUseCase
import com.example.blinknotes.domain.usecase.post.TogglePostLikeUseCase
import com.example.blinknotes.domain.usecase.post.ReportPostUseCase
import com.example.blinknotes.domain.usecase.post.GetPostByIdUseCase
import com.example.blinknotes.domain.usecase.post.CheckPostLikeStatusUseCase
import com.example.blinknotes.domain.usecase.post.TogglePostSaveUseCase
import com.example.blinknotes.domain.usecase.user.GetUserUseCase
import com.example.blinknotes.domain.usecase.user.GetAllUsersUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

sealed class ExploreScreenUiState {
    object Initial : ExploreScreenUiState()
    object Loading : ExploreScreenUiState()
    object Success : ExploreScreenUiState()
    data class Error(val error: ExploreScreenError) : ExploreScreenUiState()
}

sealed class ExploreScreenError {
    data class NetworkError(val message: String) : ExploreScreenError()
    data class DatabaseError(val message: String) : ExploreScreenError()
    data class AuthError(val message: String) : ExploreScreenError()
    data class ValidationError(val message: String) : ExploreScreenError()
}

@HiltViewModel
class ExploreScreenViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val togglePostLikeUseCase: TogglePostLikeUseCase,
    private val reportPostUseCase: ReportPostUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val checkPostLikeStatusUseCase: CheckPostLikeStatusUseCase,
    private val togglePostSaveUseCase: TogglePostSaveUseCase
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _users = MutableStateFlow<Map<String, User?>>(emptyMap())
    val users: StateFlow<Map<String, User?>> = _users.asStateFlow()

    private val _usersAll = MutableStateFlow<Map<String, User?>>(emptyMap())
    val usersAll: StateFlow<Map<String, User?>> = _usersAll.asStateFlow()

    private val _userCache = mutableMapOf<String, User?>()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _postLikeStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val postLikeStatus: StateFlow<Map<String, Boolean>> = _postLikeStatus.asStateFlow()

    private val _uiState = MutableStateFlow<ExploreScreenUiState>(ExploreScreenUiState.Initial)
    val uiState: StateFlow<ExploreScreenUiState> = _uiState.asStateFlow()

    private var lastVisiblePostId: String? = null
    private var _listUser = MutableStateFlow<List<String>>(emptyList())
    internal var isLoading = false
    private var loadedPostIds = mutableSetOf<String>()
    private var hasMorePosts = true
    private val pageSize = 20

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        viewModelScope.launch {
            getAllUser()
            loadMorePosts()
        }
    }

    fun getAllUser() {
        viewModelScope.launch {
            try {
                _uiState.value = ExploreScreenUiState.Loading
                val users = getAllUsersUseCase()
                _usersAll.value = users.associateBy { it.userId }
                _uiState.value = ExploreScreenUiState.Success
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error fetching users: ${e.message}")
                _uiState.value = ExploreScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ExploreScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ExploreScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ExploreScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun fetchUser(userId: String) {
        if (_userCache.containsKey(userId)) {
            _users.value = _users.value + (userId to _userCache[userId])
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = ExploreScreenUiState.Loading
                val user = getUserUseCase(userId)
                _userCache[userId] = user
                _users.value = _users.value + (userId to user)
                if (user != null) {
                    Log.d("ExploreScreenViewModel", "User loaded: $user")
                } else {
                    Log.e("ExploreScreenViewModel", "User not found")
                }
                _uiState.value = ExploreScreenUiState.Success
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error fetching user: ${e.message}")
                _uiState.value = ExploreScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ExploreScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ExploreScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ExploreScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun getPostByPostId(postId: String, callback: (Post?) -> Unit) {
        _posts.value.find { it.id == postId }?.let {
            callback(it)
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = ExploreScreenUiState.Loading
                val post = getPostByIdUseCase(postId)
                callback(post)
                _uiState.value = ExploreScreenUiState.Success
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error fetching post: ${e.message}")
                _uiState.value = ExploreScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ExploreScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ExploreScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ExploreScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
                callback(null)
            }
        }
    }

    fun loadMorePosts() {
        if (isLoading || !hasMorePosts) return
        isLoading = true
        viewModelScope.launch {
            try {
                _uiState.value = ExploreScreenUiState.Loading
                val (newPosts, newLastVisiblePostId) = getPostsUseCase(pageSize, lastVisiblePostId)

                if (newPosts.isNotEmpty()) {
                    _posts.value = _posts.value + newPosts
                    lastVisiblePostId = newLastVisiblePostId
                    hasMorePosts = newPosts.size == pageSize
                } else {
                    hasMorePosts = false
                }

                loadUsersForPosts(newPosts)

                _uiState.value = ExploreScreenUiState.Success
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error loading posts: ${e.message}")
                _uiState.value = ExploreScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ExploreScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ExploreScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ExploreScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadUsersForPosts(posts: List<Post>) {
        viewModelScope.launch {
            try {
                val userIds = posts.map { it.userId }.distinct()
                val usersMap = userIds.associateWith { userId ->
                    _userCache[userId] ?: getUserUseCase(userId).also { user ->
                        _userCache[userId] = user
                    }
                }
                _users.value = _users.value + usersMap
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error loading users for posts: ${e.message}")
            }
        }
    }

    fun checkPostLikeStatus(postId: String, userId: String) {
        viewModelScope.launch {
            try {
                val isLiked = checkPostLikeStatusUseCase(postId, userId)
                _postLikeStatus.value = _postLikeStatus.value.toMutableMap().apply {
                    put(postId, isLiked)
                }
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error checking post like status: ${e.message}")
                _uiState.value = ExploreScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ExploreScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ExploreScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ExploreScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun clearPosts() {
        _posts.value = emptyList()
        lastVisiblePostId = null
        hasMorePosts = true
        loadedPostIds.clear()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                clearPosts()
                loadMorePosts()
                getAllUser()
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error refreshing: ${e.message}")
                _uiState.value = ExploreScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ExploreScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ExploreScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ExploreScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
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

    fun togglePostLike(postId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Kiểm tra trạng thái like hiện tại
                val currentStatus = _postLikeStatus.value[postId] ?: false
                
                // Gọi use case để toggle like
                togglePostLikeUseCase(postId, userId)
                
                // Sau khi toggle thành công, cập nhật trạng thái trong UI
                _postLikeStatus.value = _postLikeStatus.value.toMutableMap().apply {
                    put(postId, !currentStatus)
                }
                
                // Cập nhật số lượng like trong danh sách posts
                _posts.value = _posts.value.map { post ->
                    if (post.id == postId) {
                        post.copy(
                            likesCount = if (!currentStatus) post.likesCount + 1 else post.likesCount - 1
                        )
                    } else {
                        post
                    }
                }
                
                Log.d("ExploreScreenViewModel", "Successfully toggled like for post $postId")
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error toggling post like: ${e.message}")
                _uiState.value = ExploreScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ExploreScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ExploreScreenError.DatabaseError(e.message ?: "Database error")
                        else -> ExploreScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun toggleLike(postId: String, userId: String) = togglePostLike(postId, userId)

    fun togglePostSave(postId: String, userId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ExploreScreenUiState.Loading
                togglePostSaveUseCase(postId, userId)
                _uiState.value = ExploreScreenUiState.Success
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error toggling save", e)
                _uiState.value = ExploreScreenUiState.Error(ExploreScreenError.ValidationError("Invalid operation"))
            }
        }
    }

    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ExploreScreenUiState.Loading
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    reportPostUseCase(postId, currentUser.uid, reason)
                }
                _uiState.value = ExploreScreenUiState.Success
            } catch (e: Exception) {
                Log.e("ExploreScreenViewModel", "Error reporting post", e)
                _uiState.value = ExploreScreenUiState.Error(ExploreScreenError.ValidationError("Invalid operation"))
            }
        }
    }
}
