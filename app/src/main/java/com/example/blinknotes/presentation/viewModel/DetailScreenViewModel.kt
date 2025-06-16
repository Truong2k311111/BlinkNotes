package com.example.blinknotes.presentation.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.Comment
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.repository.CommentRepository
import com.example.blinknotes.domain.repository.PostRepository
import com.example.blinknotes.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.plus
import com.example.blinknotes.domain.usecase.comment.AddCommentUseCase
import com.example.blinknotes.domain.usecase.comment.GetCommentsUseCase
import com.example.blinknotes.domain.usecase.comment.ToggleCommentLikeUseCase
import com.example.blinknotes.domain.usecase.user.GetUserUseCase
import com.example.blinknotes.domain.usecase.notification.SendFollowNotificationUseCase
import com.example.blinknotes.domain.usecase.notification.CreateSystemNotificationUseCase
import com.example.blinknotes.domain.usecase.post.ReportPostUseCase
import com.example.blinknotes.domain.usecase.post.SharePostUseCase
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.example.blinknotes.domain.usecase.comment.CheckCommentLikeStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel

sealed class DetailScreenUiState {
    object Initial : DetailScreenUiState()
    object Loading : DetailScreenUiState()
    object Success : DetailScreenUiState()
    data class Error(val error: DetailScreenError) : DetailScreenUiState()
}

sealed class DetailScreenError {
    data class NetworkError(val message: String) : DetailScreenError()
    data class DatabaseError(val message: String) : DetailScreenError()
    data class AuthError(val message: String) : DetailScreenError()
    data class ValidationError(val message: String) : DetailScreenError()
    data class UserNotFound(val message: String) : DetailScreenError()
}

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val addCommentUseCase: AddCommentUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val toggleCommentLikeUseCase: ToggleCommentLikeUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val sendFollowNotificationUseCase: SendFollowNotificationUseCase,
    private val createSystemNotificationUseCase: CreateSystemNotificationUseCase,
    private val reportPostUseCase: ReportPostUseCase,
    private val sharePostUseCase: SharePostUseCase,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val checkCommentLikeStatusUseCase: CheckCommentLikeStatusUseCase
) : ViewModel() {
    var comments by mutableStateOf<List<Comment>>(emptyList())
    private val _uiState = MutableStateFlow<DetailScreenUiState>(DetailScreenUiState.Initial)
    val uiState: StateFlow<DetailScreenUiState> = _uiState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private var lastCommentTimestamp: Long? = null
    private val pageSize = 20
    private var hasMoreComments = true

    fun addComment(postId: String, userId: String, content: String, parentCommentId: String? = null) {
        if (content.isBlank()) {
            _uiState.value = DetailScreenUiState.Error(DetailScreenError.ValidationError("Comment cannot be empty"))
            return
        }

        viewModelScope.launch {
            _uiState.value = DetailScreenUiState.Loading
            try {
                val success = addCommentUseCase(postId, userId, content, parentCommentId)
                if (success) {
                    getComments(postId)
                    _uiState.value = DetailScreenUiState.Success
                } else {
                    _uiState.value = DetailScreenUiState.Error(DetailScreenError.DatabaseError("Failed to add comment"))
                }
            } catch (e: Exception) {
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun getComments(postId: String) {
        viewModelScope.launch {
            _uiState.value = DetailScreenUiState.Loading
            try {
                comments = getCommentsUseCase(postId)
                if (comments.isNotEmpty()) {
                    lastCommentTimestamp = comments.last().createdAt
                }
                _uiState.value = DetailScreenUiState.Success
            } catch (e: Exception) {
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun loadMoreComments(postId: String) {
        if (!hasMoreComments) return
        viewModelScope.launch {
            try {
                val newComments = getCommentsUseCase(
                    postId = postId,
                    lastTimestamp = lastCommentTimestamp,
                    pageSize = pageSize
                )
                if (newComments.isNotEmpty()) {
                    lastCommentTimestamp = newComments.last().createdAt
                    comments = comments + newComments
                } else {
                    hasMoreComments = false
                }
            } catch (e: Exception) {
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Failed to load more comments")
                    }
                )
            }
        }
    }

    fun toggleCommentLike(commentId: String, userId: String) {
        viewModelScope.launch {
            try {
                toggleCommentLikeUseCase(commentId, userId)
                // Refresh comments to update like status
                comments.firstOrNull { it.id == commentId }?.postId?.let { postId ->
                    getComments(postId)
                }
            } catch (e: Exception) {
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Failed to toggle comment like")
                    }
                )
            }
        }
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = getUserUseCase(userId)
                callback(user)
            } catch (e: Exception) {
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Failed to get user")
                    }
                )
                callback(null)
            }
        }
    }

    fun getUserByBlinkNotesId(blinkNotesId: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = getUserUseCase.invokeByBlinkNotesId(blinkNotesId)
                callback(user)
            } catch (e: Exception) {
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Failed to get user")
                    }
                )
                callback(null)
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

    fun getCommentById(commentId: String, callback: (Comment?) -> Unit) {
        viewModelScope.launch {
            try {
                val comment = commentRepository.getCommentById(commentId)
                callback(comment)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    private val _followStatus = MutableStateFlow<Map<String, FollowStatus>>(emptyMap())
    val followStatus: StateFlow<Map<String, FollowStatus>> = _followStatus

    var currentPostUserId: String = ""
        private set

    data class FollowStatus(
        val isFollowing: Boolean = false,
        val isFollowedBy: Boolean = false
    )

    fun getPostsByLargeUserList(userIds: List<String>, callback: (List<Post>) -> Unit) {
        viewModelScope.launch {
            try {
                val posts = postRepository.getPostsByUserIds(userIds)
                callback(posts)
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error getting posts", e)
                callback(emptyList())
            }
        }
    }

    fun checkFollowStatus(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUser = getUserUseCase(currentUserId)
                val targetUser = getUserUseCase(targetUserId)

                if (currentUser != null && targetUser != null) {
                    val isFollowing = currentUser.following.contains(targetUserId)
                    val isFollowedBy = targetUser.following.contains(currentUserId)
                    _followStatus.value = _followStatus.value + (targetUserId to FollowStatus(isFollowing, isFollowedBy))
                }
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error checking follow status", e)
            }
        }
    }

    fun toggleFollow(userId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUser = getUserUseCase(userId)
                val targetUser = getUserUseCase(targetUserId)

                if (currentUser != null && targetUser != null) {
                    val isCurrentlyFollowing = currentUser.following.contains(targetUserId)
                    val updatedCurrentUser = currentUser.copy(
                        following = if (isCurrentlyFollowing) currentUser.following - targetUserId else currentUser.following + targetUserId
                    )
                    val updatedTargetUser = targetUser.copy(
                        followers = if (isCurrentlyFollowing) targetUser.followers - userId else targetUser.followers + userId
                    )
                    _followStatus.value = _followStatus.value + (targetUserId to FollowStatus(
                        isFollowing = updatedCurrentUser.following.contains(targetUserId),
                        isFollowedBy = updatedTargetUser.followers.contains(userId)
                    ))
                    if (!isCurrentlyFollowing) {
                        sendFollowNotificationUseCase(userId, targetUserId)
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error toggling follow status", e)
            }
        }
    }

    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            try {
                _uiState.value = DetailScreenUiState.Loading
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    reportPostUseCase(postId, currentUser.uid, reason)
                }
                _uiState.value = DetailScreenUiState.Success
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error reporting post", e)
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun updateComment(commentId: String, newContent: String) {
        viewModelScope.launch {
            try {
                val success = commentRepository.updateComment(commentId, newContent)
                if (success) {
                    // Refresh comments to show updated content
                    comments.firstOrNull { it.id == commentId }?.postId?.let { postId ->
                        getComments(postId)
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error updating comment: ${e.message}")
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                val success = commentRepository.deleteComment(commentId)
                if (success) {
                    // Remove comment from local list
                    comments = comments.filter { it.id != commentId }.map { comment ->
                        comment.copy(replies = comment.replies.filter { it.id != commentId })
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error deleting comment: ${e.message}")
            }
        }
    }

    fun sharePostWithUser(
        post: Post,
        senderId: String,
        receiverId: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                sharePostUseCase(post, senderId, receiverId)
                onSuccess?.invoke()
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error sharing post", e)
                onFailure?.invoke(e)
            }
        }
    }

    fun clearComments() {
        comments = emptyList()
        lastCommentTimestamp = null
        hasMoreComments = true
    }

    fun refreshComments(postId: String) {
        clearComments()
        getComments(postId)
    }

    fun checkCommentLikeStatus(commentId: String, userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isLiked = checkCommentLikeStatusUseCase(commentId, userId)
                callback(isLiked)
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error checking comment like status", e)
                callback(false)
            }
        }
    }

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = DetailScreenUiState.Loading
                val user = getUserUseCase(userId)
                if (user != null) {
                    _user.value = user
                    _uiState.value = DetailScreenUiState.Success
                } else {
                    _uiState.value = DetailScreenUiState.Error(DetailScreenError.UserNotFound("User not found"))
                }
            } catch (e: Exception) {
                _uiState.value = DetailScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> DetailScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> DetailScreenError.DatabaseError(e.message ?: "Database error")
                        else -> DetailScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }
}

