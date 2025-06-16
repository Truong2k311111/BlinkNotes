package com.example.blinknotes.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.usecase.user.GetCurrentUserUseCase
import com.example.blinknotes.domain.usecase.user.UpdateUserUseCase
import com.example.blinknotes.domain.usecase.user.GetUserByIdUseCase
import com.example.blinknotes.domain.usecase.post.GetPostsByUserIdsUseCase
import com.example.blinknotes.domain.usecase.user.GetAllUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowedScreenViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getPostsByUserIdsUseCase: GetPostsByUserIdsUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
) : ViewModel() {

    private val _followedUsers = MutableStateFlow<List<User>>(emptyList())
    val followedUsers: StateFlow<List<User>> = _followedUsers.asStateFlow()

    private val _suggestedUsers = MutableStateFlow<List<User>>(emptyList())
    val suggestedUsers: StateFlow<List<User>> = _suggestedUsers.asStateFlow()

    private val _followersUsers = MutableStateFlow<List<User>>(emptyList())
    val followersUsers: StateFlow<List<User>> = _followersUsers.asStateFlow()

    private val _userIdListFollowed = MutableStateFlow<List<String>>(emptyList())
    val userIdListFollowed: StateFlow<List<String>> = _userIdListFollowed.asStateFlow()

    private val _hasFollowedUsers = MutableStateFlow(false)
    val hasFollowedUsers: StateFlow<Boolean> = _hasFollowedUsers.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var selectedUserId: String? = null

    init {
        loadFollowedUsers()
        loadSuggestedUsers()
        loadFollowersUsers()
    }

     fun loadFollowedUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUser = getCurrentUserUseCase()
                if (currentUser == null) {
                    _error.value = "Không thể lấy thông tin người dùng"
                    return@launch
                }

                val followingIds = currentUser.following
                _userIdListFollowed.value = followingIds
                _hasFollowedUsers.value = followingIds.isNotEmpty()

                val users = followingIds.mapNotNull { userId ->
                    getUserByIdUseCase(userId)
                }

                _followedUsers.value = users
                loadPostsForUsers(followingIds)
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải danh sách người dùng đang theo dõi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

     fun loadSuggestedUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUser = getCurrentUserUseCase()
                if (currentUser == null) {
                    _error.value = "Không thể lấy thông tin người dùng"
                    return@launch
                }

                // Lấy danh sách user đã follow
                val followingIds = currentUser.following.toSet()
                
                // Lấy tất cả user trong hệ thống
                val allUsers = getAllUsersUseCase()
                
                // Lọc ra những user chưa được follow và không phải là chính mình
                val suggestedUsers = allUsers.filter { user ->
                    user.userId != currentUser.userId && !followingIds.contains(user.userId)
                }

                // Sắp xếp theo số lượng followers (để hiển thị những user phổ biến trước)
                val sortedSuggestedUsers = suggestedUsers.sortedByDescending { it.followers.size }

                // Giới hạn số lượng user đề xuất
                _suggestedUsers.value = sortedSuggestedUsers.take(10)
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải danh sách người dùng đề xuất: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

     fun loadFollowersUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUser = getCurrentUserUseCase()
                if (currentUser == null) {
                    _error.value = "Không thể lấy thông tin người dùng"
                    return@launch
                }

                val followersIds = currentUser.followers
                val users = followersIds.mapNotNull { userId ->
                    getUserByIdUseCase(userId)
                }

                _followersUsers.value = users
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải danh sách người theo dõi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadPostsForUsers(userIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val posts = getPostsByUserIdsUseCase(userIds)
                _posts.value = posts
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải bài viết: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUser = getCurrentUserUseCase()
                if (currentUser == null) {
                    _error.value = "Không thể lấy thông tin người dùng"
                    return@launch
                }

                val updatedFollowing = currentUser.following.toMutableList().apply {
                    if (!contains(userId)) {
                        add(userId)
                    }
                }

                updateUserUseCase(
                    currentUser.copy(
                        following = updatedFollowing
                    )
                ).onSuccess {
                    loadFollowedUsers()
                    loadSuggestedUsers()
                }.onFailure { e ->
                    _error.value = "Lỗi khi cập nhật thông tin người dùng: ${e.message}"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi theo dõi người dùng: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUser = getCurrentUserUseCase()
                if (currentUser == null) {
                    _error.value = "Không thể lấy thông tin người dùng"
                    return@launch
                }

                val updatedFollowing = currentUser.following.toMutableList().apply {
                    remove(userId)
                }

                updateUserUseCase(
                    currentUser.copy(
                        following = updatedFollowing
                    )
                )

                loadFollowedUsers()
                loadSuggestedUsers()
            } catch (e: Exception) {
                _error.value = "Lỗi khi bỏ theo dõi người dùng: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadFollowedUsers()
        loadSuggestedUsers()
        loadFollowersUsers()
    }
    fun setSelectedUserId(userId: String) {
        selectedUserId = userId
    }
}