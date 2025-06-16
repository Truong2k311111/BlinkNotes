package com.example.blinknotes.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.ActivityNotification
import com.example.blinknotes.domain.usecase.notification.GetActivityNotificationsUseCase
import com.example.blinknotes.domain.usecase.notification.MarkActivityNotificationAsReadUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ActivityNotificationUiState {
    object Initial : ActivityNotificationUiState()
    object Loading : ActivityNotificationUiState()
    object Success : ActivityNotificationUiState()
    data class Error(val error: ActivityNotificationError) : ActivityNotificationUiState()
}

sealed class ActivityNotificationError {
    data class NetworkError(val message: String) : ActivityNotificationError()
    data class DatabaseError(val message: String) : ActivityNotificationError()
    data class AuthError(val message: String) : ActivityNotificationError()
    data class ValidationError(val message: String) : ActivityNotificationError()
}

@HiltViewModel
class ActivityNotificationViewModel @Inject constructor(
    private val getActivityNotificationsUseCase: GetActivityNotificationsUseCase,
    private val markActivityNotificationAsReadUseCase: MarkActivityNotificationAsReadUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ActivityNotificationUiState>(ActivityNotificationUiState.Initial)
    val uiState: StateFlow<ActivityNotificationUiState> = _uiState.asStateFlow()

    private val _notifications = MutableStateFlow<List<ActivityNotification>>(emptyList())
    val notifications: StateFlow<List<ActivityNotification>> = _notifications.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                _uiState.value = ActivityNotificationUiState.Loading
                val notifications = getActivityNotificationsUseCase(currentUserId)
                _notifications.value = notifications
                Log.d("ActivityNotificationViewModel", "Fetched notifications: $notifications")
                _uiState.value = ActivityNotificationUiState.Success
            } catch (e: Exception) {
                Log.e("ActivityNotificationViewModel", "Error fetching notifications", e)
                _uiState.value = ActivityNotificationUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ActivityNotificationError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ActivityNotificationError.DatabaseError(e.message ?: "Database error")
                        else -> ActivityNotificationError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ActivityNotificationUiState.Loading
                markActivityNotificationAsReadUseCase(notificationId)
                fetchNotifications()
                _uiState.value = ActivityNotificationUiState.Success
            } catch (e: Exception) {
                _uiState.value = ActivityNotificationUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> ActivityNotificationError.AuthError(e.message ?: "Authentication error")
                        is FirebaseFirestoreException -> ActivityNotificationError.DatabaseError(e.message ?: "Database error")
                        else -> ActivityNotificationError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                fetchNotifications()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}