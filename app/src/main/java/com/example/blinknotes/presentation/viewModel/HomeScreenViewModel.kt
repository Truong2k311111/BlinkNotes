package com.example.blinknotes.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.HomeScreenViewModelData
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.usecase.post.GetPostsUseCase
import com.example.blinknotes.domain.usecase.user.GetUserUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

sealed class HomeScreenUiState {
    object Initial : HomeScreenUiState()
    object Loading : HomeScreenUiState()
    object Success : HomeScreenUiState()
    data class Error(val error: HomeScreenError) : HomeScreenUiState()
}

sealed class HomeScreenError {
    data class NetworkError(val message: String) : HomeScreenError()
    data class DatabaseError(val message: String) : HomeScreenError()
    data class AuthError(val message: String) : HomeScreenError()
    data class ValidationError(val message: String) : HomeScreenError()
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Initial)
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _users = MutableStateFlow<Map<String, User?>>(emptyMap())
    val users: StateFlow<Map<String, User?>> = _users.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var lastVisiblePost: Post? = null
    private var hasMorePosts = true
    private val pageSize = 10

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    init {
//        loadPosts()
    }

//    fun loadPosts() {
//        viewModelScope.launch {
//            try {
//                _uiState.value = HomeScreenUiState.Loading
//                val posts = getPostsUseCase(pageSize, lastVisiblePost?.id as Post?)
//                _posts.value = posts
//                loadUsersForPosts(posts)
//                _uiState.value = HomeScreenUiState.Success
//            } catch (e: Exception) {
//                _uiState.value = HomeScreenUiState.Error(
//                    when (e) {
//                        is FirebaseAuthException -> HomeScreenError.AuthError(e.message ?: "Authentication error")
//                        is FirebaseFirestoreException -> HomeScreenError.DatabaseError(e.message ?: "Database error")
//                        else -> HomeScreenError.NetworkError(e.message ?: "Unknown error")
//                    }
//                )
//            }
//        }
//    }

    private fun loadUsersForPosts(posts: List<Post>) {
        viewModelScope.launch {
            try {
                val userIds = posts.map { it.userId }.distinct()
                val users = userIds.associateWith { userId ->
                    getUserUseCase(userId)
                }
                _users.value = users
            } catch (e: Exception) {
                _uiState.value = HomeScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> HomeScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> HomeScreenError.DatabaseError(e.message ?: "Database error")
                        else -> HomeScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                lastVisiblePost = null
                hasMorePosts = true
//                loadPosts()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}