package com.example.blinknotes.ui.notify

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class StatusPost(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val imageUri: Uri? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val comments: List<Comment> = emptyList()
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String
)

sealed class StatusScreenState {
    object Loading : StatusScreenState()
    data class Success(val posts: List<StatusPost>) : StatusScreenState()
    data class Error(val message: String) : StatusScreenState()
}

class StatusScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StatusScreenState>(StatusScreenState.Loading)
    val uiState: StateFlow<StatusScreenState> = _uiState.asStateFlow()

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var postContent by mutableStateOf("")
        private set

    var isPosting by mutableStateOf(false)
        private set

    private val _posts = MutableStateFlow<List<StatusPost>>(emptyList())
    val posts: StateFlow<List<StatusPost>> = _posts.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                // TODO: Implement actual data loading from repository
                _uiState.value = StatusScreenState.Success(emptyList())
            } catch (e: Exception) {
                _uiState.value = StatusScreenState.Error(e.message ?: "Failed to load posts")
            }
        }
    }

    fun updatePostContent(content: String) {
        postContent = content
    }

    fun setSelectedImage(uri: Uri?) {
        selectedImageUri = uri
    }

    fun createPost() {
        if (postContent.isBlank() && selectedImageUri == null) return

        viewModelScope.launch {
            isPosting = true
            try {
                val newPost = StatusPost(
                    content = postContent,
                    imageUri = selectedImageUri
                )
                _posts.value = _posts.value + newPost
                clearPostDraft()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isPosting = false
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            _posts.value = _posts.value.map { post ->
                if (post.id == postId) {
                    post.copy(likes = post.likes + 1)
                } else post
            }
        }
    }

    fun addComment(postId: String, commentContent: String, userId: String) {
        viewModelScope.launch {
            _posts.value = _posts.value.map { post ->
                if (post.id == postId) {
                    val newComment = Comment(
                        content = commentContent,
                        userId = userId
                    )
                    post.copy(comments = post.comments + newComment)
                } else post
            }
        }
    }

    private fun clearPostDraft() {
        postContent = ""
        selectedImageUri = null
    }
} 