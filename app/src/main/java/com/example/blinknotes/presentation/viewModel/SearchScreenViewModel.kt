package com.example.blinknotes.presentation.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.usecase.post.SearchPostsUseCase
import com.example.blinknotes.domain.usecase.user.GetUserUseCase
import com.example.blinknotes.domain.usecase.user.SearchUsersUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchScreenUiState {
    object Initial : SearchScreenUiState()
    object Loading : SearchScreenUiState()
    object Success : SearchScreenUiState()
    data class Error(val error: SearchScreenError) : SearchScreenUiState()
}

sealed class SearchScreenError {
    data class NetworkError(val message: String) : SearchScreenError()
    data class DatabaseError(val message: String) : SearchScreenError()
    data class AuthError(val message: String) : SearchScreenError()
    data class ValidationError(val message: String) : SearchScreenError()
}

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val searchPostsUseCase: SearchPostsUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val getUserUseCase: GetUserUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchScreenUiState>(SearchScreenUiState.Initial)
    val uiState: StateFlow<SearchScreenUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Post>>(emptyList())
    val searchResults: StateFlow<List<Post>> = _searchResults.asStateFlow()

    private val _userResults = MutableStateFlow<List<User>>(emptyList())
    val userResults: StateFlow<List<User>> = _userResults.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val sharedPreferences = context.getSharedPreferences("blinknotes_prefs", Context.MODE_PRIVATE)
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _users = MutableStateFlow<Map<String, User?>>(emptyMap())
    val users: StateFlow<Map<String, User?>> = _users.asStateFlow()

    private val _error = MutableStateFlow<SearchScreenError?>(null)
    val error: StateFlow<SearchScreenError?> = _error.asStateFlow()

    init {
        loadSearchHistory()
    }
    fun getUserById(userId: String): User? {
        if (_users.value.isEmpty()) {
            viewModelScope.launch {
                try {
                    val user = getUserUseCase(userId)
                    _users.value = _users.value + (userId to user)
                } catch (e: Exception) {
                    // Handle error if needed
                }
            }
        }
        return _users.value[userId]
    }

    private fun loadSearchHistory() {
        val historyString = sharedPreferences.getString("search_history_$currentUserId", "")
        if (!historyString.isNullOrEmpty()) {
            _searchHistory.value = historyString.split(",")
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _userResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = SearchScreenUiState.Loading
                val posts = searchPostsUseCase(query)
                val users = searchUsersUseCase(query)
                _searchResults.value = posts
                _userResults.value = users
                _uiState.value = SearchScreenUiState.Success
                addSearchHistory(query)
            } catch (e: Exception) {
                _uiState.value = SearchScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> SearchScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> SearchScreenError.DatabaseError(e.message ?: "Database error")
                        else -> SearchScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun addSearchHistory(keyword: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        if (!currentHistory.contains(keyword)) {
            currentHistory.add(0, keyword)
            if (currentHistory.size > 10) {
                currentHistory.removeAt(currentHistory.size - 1)
            }
            _searchHistory.value = currentHistory
            saveSearchHistory()
        }
    }

    fun removeSearchHistory(keyword: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(keyword)
        _searchHistory.value = currentHistory
        saveSearchHistory()
    }

    private fun saveSearchHistory() {
        val historyString = _searchHistory.value.joinToString(",")
        sharedPreferences.edit().putString("search_history_$currentUserId", historyString).apply()
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        sharedPreferences.edit().remove("search_history_$currentUserId").apply()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                loadSearchHistory()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SearchScreenUiState.Loading
                val results = searchUsersUseCase(query)
                _userResults.value = results
                _uiState.value = SearchScreenUiState.Success
            } catch (e: Exception) {
                _uiState.value = SearchScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> SearchScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> SearchScreenError.DatabaseError(e.message ?: "Database error")
                        else -> SearchScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun searchPosts(query: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SearchScreenUiState.Loading
                val results = searchPostsUseCase(query)
                _searchResults.value = results
                _uiState.value = SearchScreenUiState.Success
            } catch (e: Exception) {
                _uiState.value = SearchScreenUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> SearchScreenError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> SearchScreenError.DatabaseError(e.message ?: "Database error")
                        else -> SearchScreenError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun getSearchHistory(): List<String> {
        if (_searchHistory.value.isEmpty()) {
            loadSearchHistory()
        }
        return _searchHistory.value
    }

    fun searchPostsByHashtag(hashtag: String) {
        Log.d("SearchScreenViewModel", "Searching posts by hashtag: $hashtag")
        viewModelScope.launch {
            try {
                _uiState.value = SearchScreenUiState.Loading
                _error.value = null

                // Lấy tất cả bài viết
                val allPosts = searchPostsUseCase("")
                Log.d("SearchScreenViewModel", "Total posts found: ${allPosts.size}")
                
                // Lọc bài viết theo hashtag trong cả caption và content
                val filteredPosts = allPosts.filter { post ->
                    val captionContainsHashtag = post.caption?.let { caption ->
                        val contains = caption.contains(hashtag, ignoreCase = true) ||
                            caption.split(" ").any { it.equals(hashtag, ignoreCase = true) }
                        if (contains) {
                            Log.d("SearchScreenViewModel", "Found hashtag in caption: ${post.caption}")
                        }
                        contains
                    } ?: false

                    val contentContainsHashtag = post.content?.let { content ->
                        val contains = content.contains(hashtag, ignoreCase = true) ||
                            content.split(" ").any { it.equals(hashtag, ignoreCase = true) }
                        if (contains) {
                            Log.d("SearchScreenViewModel", "Found hashtag in content: ${post.content}")
                        }
                        contains
                    } ?: false

                    captionContainsHashtag || contentContainsHashtag
                }

                Log.d("SearchScreenViewModel", "Filtered posts count: ${filteredPosts.size}")
                _searchResults.value = filteredPosts
                _uiState.value = SearchScreenUiState.Success
            } catch (e: Exception) {
                Log.e("SearchScreenViewModel", "Error searching posts: ${e.message}")
                _error.value = SearchScreenError.NetworkError("Lỗi khi tìm kiếm bài viết: ${e.message}")
                _uiState.value = SearchScreenUiState.Error(_error.value!!)
            }
        }
    }
}