package com.example.blinknotes.ui.notify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.ui.home.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotifyViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _notifyItems = MutableStateFlow<List<User>>(emptyList())
    val notifyItems: StateFlow<List<User>> = _notifyItems

    private val _isActive = MutableStateFlow(true)
    val isActive: StateFlow<Boolean> = _isActive

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
      //  fetchNotifyItems()
        fetchUsers() // Fetch users on initialization
        fetchCurrentUser() // Fetch the logged-in user on initialization
    }

    private fun fetchCurrentUser() {
        // Replace "currentUserId" with the actual logic to get the logged-in user's ID
        val currentUserId = "currentUserId"
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                _currentUser.value = user
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    fun fetchNotifyItems() {
        firestore.collection("notifications")
            .get()
            .addOnSuccessListener { result ->
                val items = result.map { document ->
                    User(
                        username = document.getString("username") ?: "",
                        profileImage = document.getString("profileImage") ?: "",
                        hasMoment = document.getBoolean("hasMoment") ?: false,
                        isMomentSeen = document.getBoolean("isMomentSeen") ?: false,
                        isOnline = document.getBoolean("isOnline") ?: false,
                        latestMessage = document.getString("latestMessage") ?: "",
                        unreadMessages = document.getLong("unreadMessages")?.toInt() ?: 0,
                        blinkNotesId = document.getString("blinkNotesId") ?: "",
                    )
                }
                _notifyItems.value = items
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }

    fun fetchUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userList = result.map { document ->
                    User(
                        username = document.getString("username") ?: "",
                        profileImage = document.getString("profileImage") ?: "",
                        hasMoment = document.getBoolean("hasMoment") ?: false,
                        isMomentSeen = document.getBoolean("isMomentSeen") ?: false,
                        isOnline = document.getBoolean("isOnline") ?: false,
                        latestMessage = document.getString("latestMessage") ?: "",
                        unreadMessages = document.getLong("unreadMessages")?.toInt() ?: 0,
                        blinkNotesId = document.getString("blinkNotesId") ?: "",
                    )
                }
                _users.value = userList
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    fun toggleActiveStatus() {
        val currentUserId = "currentUserId" // Replace with actual logic to get the logged-in user's ID
        val newStatus = !_isActive.value
        _isActive.value = newStatus

        firestore.collection("users")
            .document(currentUserId)
            .update("isOnline", newStatus)
            .addOnSuccessListener {
                // Successfully updated status in Firestore
            }
            .addOnFailureListener {
                // Handle failure, revert the status locally if needed
                _isActive.value = !newStatus
            }
    }
}
