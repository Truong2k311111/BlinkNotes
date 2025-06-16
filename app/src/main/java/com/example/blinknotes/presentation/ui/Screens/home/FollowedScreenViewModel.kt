package com.example.blinknotes.presentation.ui.Screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowedScreenViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _followedUsers = MutableStateFlow<List<User>>(emptyList())
    val followedUsers: StateFlow<List<User>> = _followedUsers.asStateFlow()

    private val _suggestedUsers = MutableStateFlow<List<User>>(emptyList())
    val suggestedUsers: StateFlow<List<User>> = _suggestedUsers.asStateFlow()

    private val _followersUsers = MutableStateFlow<List<User>>(emptyList())
    val followersUsers: StateFlow<List<User>> = _followersUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var selectedUserId: String? = null

    fun setSelectedUserId(userId: String) {
        selectedUserId = userId
    }

    fun loadFollowersUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = selectedUserId ?: return@launch
                val followers = userRepository.getFollowers(userId)
                _followersUsers.value = followers
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFollowedUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val followed = userRepository.getFollowedUsers()
                _followedUsers.value = followed
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSuggestedUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val suggested = userRepository.getSuggestedUsers()
                _suggestedUsers.value = suggested
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 