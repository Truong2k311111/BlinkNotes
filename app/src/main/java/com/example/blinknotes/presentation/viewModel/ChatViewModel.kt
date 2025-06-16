package com.example.blinknotes.presentation.viewModel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.domain.model.Message
import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.ChatRepository
import com.example.blinknotes.domain.repository.UserRepository
import com.example.blinknotes.domain.usecase.chat.*
import com.example.blinknotes.domain.usecase.notification.CreateSystemNotificationUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

sealed class ChatState {
    data class Success(val messages: List<Message>) : ChatState()
    data class Error(val error: ChatError) : ChatState()
    object Loading : ChatState()
}

sealed class MessageStatus {
    object Sending : MessageStatus()
    object Sent : MessageStatus()
    object Delivered : MessageStatus()
    object Read : MessageStatus()
    data class Error(val error: ChatError) : MessageStatus()
}

sealed class ChatError {
    data class NetworkError(val message: String) : ChatError()
    data class StorageError(val message: String) : ChatError()
    data class AuthError(val message: String) : ChatError()
    data class ValidationError(val message: String) : ChatError()
}

class ChatViewModelFactory(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val getUnreadMessageCountUseCase: GetUnreadMessageCountUseCase,
    private val createSystemNotificationUseCase: CreateSystemNotificationUseCase,

    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                sendMessageUseCase,
                getMessagesUseCase,
                markMessagesAsReadUseCase,
                getUnreadMessageCountUseCase,
                createSystemNotificationUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val getUnreadMessageCountUseCase: GetUnreadMessageCountUseCase,
    private val createSystemNotificationUseCase: CreateSystemNotificationUseCase,

    ) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _unreadMessagesCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessagesCount: StateFlow<Map<String, Int>> = _unreadMessagesCount

    private val _isSendImage = MutableStateFlow(false)
    val isSendImage: StateFlow<Boolean> = _isSendImage

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loadingMessages = MutableStateFlow(true)
    val loadingMessages: StateFlow<Boolean> = _loadingMessages

    private val _postPreviews = MutableStateFlow<Map<Long, Map<String, Any>>>(emptyMap())
    val postPreviews: StateFlow<Map<Long, Map<String, Any>>> = _postPreviews

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState: StateFlow<ChatState> = _chatState

    private val _messageStatus = MutableStateFlow<Map<String, MessageStatus>>(emptyMap())
    val messageStatus: StateFlow<Map<String, MessageStatus>> = _messageStatus

    private var lastMessageTimestamp: Long? = null
    private val pageSize = 20
    private var hasMoreMessages = true

    private val storageRef = FirebaseStorage.getInstance().reference
    private val _notificationState = MutableStateFlow<NotificationState>(NotificationState.Loading)
    val notificationState: StateFlow<NotificationState> = _notificationState



    fun createImageUri(context: Context): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    fun updateSelectedImages(uris: List<Uri>) {
        _selectedImages.value = uris
    }

    fun addSelectedImages(uris: List<Uri>) {
        _selectedImages.value = _selectedImages.value + uris
    }

    fun clearSelectedImages() {
        _selectedImages.value = emptyList()
        _isSendImage.value = false
    }

    fun uploadAndSendMessage(
        senderId: String,
        receiverId: String,
        message: String,
        imageUris: List<Uri>,
        contentSendImage: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uploadedUrls = mutableListOf<String>()
        _isLoading.value = true
        _isSendImage.value = true
        viewModelScope.launch {
            try {
                for (uri in imageUris) {
                    val fileRef = storageRef.child("uploads/${UUID.randomUUID()}.jpg")
                    fileRef.putFile(uri).await()
                    val downloadUri = fileRef.downloadUrl.await().toString()
                    uploadedUrls.add(downloadUri)
                }
                sendMessage(senderId, receiverId, message, uploadedUrls, contentSendImage)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _chatState.value = ChatState.Error(ChatError.NetworkError(e.message ?: "Failed to upload images"))
                onFailure(e)
            }
        }
    }

    fun sendMessage(
        senderId: String,
        receiverId: String,
        message: String,
        imageUrls: List<String> = emptyList(),
        contentSendImage: String = ""
    ) {
        if (message.isBlank() && imageUrls.isEmpty()) {
            _chatState.value = ChatState.Error(ChatError.ValidationError("Message cannot be empty"))
            return
        }

        val messageId = UUID.randomUUID().toString()
        _messageStatus.value = _messageStatus.value.toMutableMap().apply {
            put(messageId, MessageStatus.Sending)
        }

        viewModelScope.launch {
            try {
                val result = sendMessageUseCase(
                    senderId = senderId,
                    receiverId = receiverId,
                    content = message,
                    imageUrls = imageUrls,
                    contentSendImage = contentSendImage
                )
                result.fold(
                    onSuccess = { message ->
                        _messageStatus.value = _messageStatus.value.toMutableMap().apply {
                            put(messageId, MessageStatus.Sent)
                        }
                    },
                    onFailure = { error ->
                        _messageStatus.value = _messageStatus.value.toMutableMap().apply {
                            put(messageId, MessageStatus.Error(ChatError.NetworkError(error.message ?: "Failed to send message")))
                        }
                        _chatState.value = ChatState.Error(ChatError.NetworkError(error.message ?: "Failed to send message"))
                    }
                )
            } catch (e: Exception) {
                _messageStatus.value = _messageStatus.value.toMutableMap().apply {
                    put(messageId, MessageStatus.Error(ChatError.NetworkError(e.message ?: "Unknown error")))
                }
                _chatState.value = ChatState.Error(ChatError.NetworkError(e.message ?: "Failed to send message"))
            }
        }
    }

    fun listenForMessages(currentUserId: String, otherUserId: String) {
        _loadingMessages.value = true
        _chatState.value = ChatState.Loading

        val db = FirebaseFirestore.getInstance()

        // Lắng nghe tất cả tin nhắn trong collection contentchat
        val listener = db.collection("contentchat")
            .orderBy("timestamp", Query.Direction.ASCENDING) // ASCENDING để load từ cũ tới mới
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _chatState.value = ChatState.Error(ChatError.NetworkError(error.message ?: "Failed to load messages"))
                    _loadingMessages.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val senderId = doc.getString("senderId") ?: return@mapNotNull null
                            val receiverId = doc.getString("receiverId") ?: return@mapNotNull null

                            // Chỉ lấy tin nhắn giữa 2 người
                            if (
                                (senderId == currentUserId && receiverId == otherUserId) ||
                                (senderId == otherUserId && receiverId == currentUserId)
                            ) {
                                Message(
                                    id = doc.id,
                                    senderId = senderId,
                                    receiverId = receiverId,
                                    content = doc.getString("content") ?: "",
                                    imageUrls = (doc.get("imageUrls") as? List<String>) ?: emptyList(),
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isRead = doc.getBoolean("isRead") ?: false
                                )
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }

                    _messages.value = messages
                    _chatState.value = ChatState.Success(messages)
                    _loadingMessages.value = false
                }
            }

        // Gọi hàm đánh dấu đã đọc (nếu cần)
        viewModelScope.launch {
            try {
                markMessagesAsRead(currentUserId, otherUserId)
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(ChatError.NetworkError(e.message ?: "Failed to mark messages as read"))
            }
        }
    }

    // Thêm hàm để hủy listener khi không cần thiết
//    override fun onCleared() {
//        super.onCleared()
//        // Hủy listener khi ViewModel bị hủy
//        FirebaseFirestore.getInstance()
//            .collection("contentchat")
//            .whereIn("participants", listOf(
//                listOf(FirebaseAuth.getInstance().currentUser?.uid ?: "", ""),
//                listOf("", FirebaseAuth.getInstance().currentUser?.uid ?: "")
//            ))
//            .remove()
//    }
    fun loadMoreMessages(currentUserId: String, otherUserId: String) {
        if (_loadingMessages.value || !hasMoreMessages) return
        viewModelScope.launch {
            try {
                _loadingMessages.value = true
                val newMessages = getMessagesUseCase(
                    currentUserId = currentUserId,
                    otherUserId = otherUserId
                )
                if (newMessages.isNotEmpty()) {
                    lastMessageTimestamp = newMessages.last().timestamp
                    _messages.value = _messages.value + newMessages
                } else {
                    hasMoreMessages = false
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(ChatError.NetworkError(e.message ?: "Failed to load more messages"))
            } finally {
                _loadingMessages.value = false
            }
        }
    }

    fun markMessagesAsRead(senderId: String, receiverId: String) {
        viewModelScope.launch {
            try {
                markMessagesAsReadUseCase(senderId, receiverId)
                fetchUnreadMessagesCount()
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(ChatError.NetworkError(e.message ?: "Failed to mark messages as read"))
            }
        }
    }

    fun fetchUnreadMessagesCount() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val unreadCounts = getUnreadMessageCountUseCase(currentUserId)
                _unreadMessagesCount.value = unreadCounts
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(ChatError.NetworkError(e.message ?: "Failed to fetch unread count"))
            }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        _postPreviews.value = emptyMap()
        lastMessageTimestamp = null
        hasMoreMessages = true
    }

    fun isMessageFromCurrentUser(message: Message): Boolean {
        return message.senderId == FirebaseAuth.getInstance().currentUser?.uid
    }

    fun refreshMessages(currentUserId: String, otherUserId: String) {
        clearMessages()
        listenForMessages(currentUserId, otherUserId)
    }
    fun reportUser(userId: String, reason: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            try {
                val reporterDoc = FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val reporterName = reporterDoc.getString("username") ?: ""
                val reporterImage = reporterDoc.getString("profileImage") ?: ""

                val reportedUserDoc = FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .get()
                    .await()

                val reportedName = reportedUserDoc.getString("username") ?: ""
                val reportedImage = reportedUserDoc.getString("profileImage") ?: ""

                createSystemNotification(
                    type = NotificationType.USER_REPORTED,
                    title = "Báo cáo người dùng",
                    content = "Người dùng bị báo cáo với lý do: $reason",
                    reporterId = currentUser.uid,
                    reporterName = reporterName,
                    reporterImage = reporterImage,
                    reportedId = userId,
                    reportedName = reportedName,
                    reportedImage = reportedImage,
                    reportReason = reason
                )
            } catch (e: Exception) {
                _notificationState.value = NotificationState.Error(NotificationError.NetworkError("Failed to report user"))
            }
        }
    }
    fun createSystemNotification(
        type: NotificationType,
        title: String,
        content: String,
        reporterId: String = "",
        reporterName: String = "",
        reporterImage: String = "",
        reportedId: String = "",
        reportedName: String = "",
        reportedImage: String = "",
        reportReason: String = ""
    ) {
        viewModelScope.launch {
            try {
                createSystemNotificationUseCase(
                    type = type,
                    title = title,
                    content = content,
                    reporterId = reporterId,
                    reporterName = reporterName,
                    reporterImage = reporterImage,
                    reportedId = reportedId,
                    reportedName = reportedName,
                    reportedImage = reportedImage,
                    reportReason = reportReason
                )
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error creating system notification", e)
            }
        }
    }
}