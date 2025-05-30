package com.example.blinknotes.ui.notify

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.ui.home.User
import com.example.blinknotes.ui.notify.activity.ActivityNotification
import com.example.blinknotes.ui.notify.notificationSysTem.NotificationType
import com.example.blinknotes.ui.notify.notificationSysTem.SystemNotification
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.UUID


data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val contentSendImage: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
)

class NotifyViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _systemNotifications = MutableStateFlow<List<SystemNotification>>(emptyList())
    val systemNotifications: StateFlow<List<SystemNotification>> = _systemNotifications

    private val _unreadSystemNotifications = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val unreadSystemNotifications: StateFlow<Map<String, Boolean>> = _unreadSystemNotifications

    private val _unreadActivityNotifications = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val unreadActivityNotifications: StateFlow<Map<String, Boolean>> = _unreadActivityNotifications

    private val _latestSystemNotification = MutableStateFlow<SystemNotification?>(null)
    val latestSystemNotification: StateFlow<SystemNotification?> = _latestSystemNotification

    private val _latestActivityNotification = MutableStateFlow<ActivityNotification?>(null)
    val latestActivityNotification: StateFlow<ActivityNotification?> = _latestActivityNotification

    private val _notifyItems = MutableStateFlow<List<User>>(emptyList())
    val notifyItems: StateFlow<List<User>> = _notifyItems

    private val _isActive = MutableStateFlow(true)
    val isActive: StateFlow<Boolean> = _isActive

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _isFriend = MutableStateFlow(false)
    val isFriend: StateFlow<Boolean> = _isFriend

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _usersFriend = MutableStateFlow<List<User>>(emptyList())
    val usersFriend: StateFlow<List<User>> = _usersFriend

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _unreadMessagesCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessagesCount: StateFlow<Map<String, Int>> = _unreadMessagesCount

    private val _isSendImage = MutableStateFlow(false)
    val  isSendImage: StateFlow<Boolean> = _isSendImage

    private val _lastMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastMessages: StateFlow<Map<String, String>> = _lastMessages

    private val _lastMessagesContentSendImage = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastMessagesContentSendImage: StateFlow<Map<String, String>> = _lastMessagesContentSendImage

    private val storageRef = FirebaseStorage.getInstance().reference


    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loadingMessages = MutableStateFlow(true)
    val loadingMessages: StateFlow<Boolean> = _loadingMessages
    private val _postPreviews = MutableStateFlow<Map<Long, Map<String, Any>>>(emptyMap())
    val postPreviews: StateFlow<Map<Long, Map<String, Any>>> = _postPreviews


    fun updateSelectedImages(uris: List<Uri>) {
        _selectedImages.value = uris
    }
    fun addSelectedImages(uris: List<Uri>) {
        _selectedImages.value = _selectedImages.value+uris
    }

    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("https://blinknotes-api.onrender.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()
    init {
        viewModelScope.launch {
            Firebase.messaging.subscribeToTopic("chat").await()
        }
    }
    init {
        fetchUsers()
        fetchCurrentUser()
        syncActiveStatusFromFirestore()
        fetchUsersFriend()
    }
    init {
        fetchSystemNotifications()
        fetchUnreadSystemNotifications()
        fetchUnreadActivityNotifications()
        fetchLatestSystemNotification()
        fetchLatestActivityNotification()
    }

    fun createImageUri(context: Context): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                for (uri in imageUris) {
                    val fileRef = storageRef.child("uploads/${UUID.randomUUID()}.jpg")
                    fileRef.putFile(uri).await()
                    val downloadUri = fileRef.downloadUrl.await().toString()
                    uploadedUrls.add(downloadUri)
                }
                sendMessage(senderId, receiverId, message, imageUrls = uploadedUrls, contentSendImage = contentSendImage)
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    onSuccess()
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    onFailure(e)
                }
            }
        }
    }
    private fun fetchCurrentUser() {
        val currentUserId =
            FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("users")
            .document(currentUserId.toString())
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                _currentUser.value = user
            }
            .addOnFailureListener {
            }
    }
    private fun syncActiveStatusFromFirestore() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val onlineStatus = document.getBoolean("isOnline") ?: false
                _isActive.value = onlineStatus
            }
            .addOnFailureListener {
            }
    }
    fun fetchLastMessageForUser(userId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("contentchat")
            .whereEqualTo("senderId", userId)
            .whereEqualTo("receiverId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                val lastMessage = documents.firstOrNull()?.getString("content") ?: ""
                _lastMessages.value = _lastMessages.value.toMutableMap().apply {
                    this[userId] = lastMessage
                }
                val contentSendImage = documents.firstOrNull()?.getString("contentSendImage") ?: ""
                _lastMessagesContentSendImage.value = _lastMessagesContentSendImage.value.toMutableMap().apply {
                    this[userId] = contentSendImage
                }

                Log.d("FETCH_LAST_MESSAGE", "Last message for user $userId: $lastMessage")
            }
            .addOnFailureListener {
                Log.e("FETCH_LAST_MESSAGE", "Failed to fetch last message for user $userId", it)
            }
    }
    fun fetchUnreadMessagesCountForAllUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("contentchat")
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { result ->
                val unreadCounts = mutableMapOf<String, Int>()
                result.documents.forEach { document ->
                    val senderId = document.getString("senderId") ?: return@forEach
                    unreadCounts[senderId] = unreadCounts.getOrDefault(senderId, 0) + 1
                }
                _unreadMessagesCount.value = unreadCounts
            }
            .addOnFailureListener { exception ->
                Log.e("FETCH_UNREAD", "Failed to fetch unread messages", exception)
            }
    }
    fun markMessagesAsRead(senderId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("contentchat")
            .whereEqualTo("senderId", senderId)
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { result ->
                val batch = firestore.batch()
                result.documents.forEach { document ->
                    batch.update(document.reference, "isRead", true)
                }
                batch.commit().addOnSuccessListener {
                    Log.d("MARK_AS_READ", "Messages marked as read for user $senderId")
                    fetchUnreadMessagesCountForAllUsers()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MARK_AS_READ", "Failed to mark messages as read for user $senderId", exception)
            }
    }

    fun fetchUsersFriend() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userList = result.mapNotNull { document ->
                    if (document.id == currentUserId) {
                        null
                    } else {
                        val userId = document.id
                        val followingRaw = document.get("following") as? List<*> ?: emptyList<Any>()
                        val following = followingRaw.filterIsInstance<String>()
                        val isFriend = following.contains(currentUserId)
                        if (isFriend) {
                            User(
                                userId = document.id,
                                username = document.getString("username") ?: "",
                                profileImage = document.getString("profileImage") ?: "",
                                hasMoment = document.getBoolean("hasMoment") ?: false,
                                isMomentSeen = document.getBoolean("isMomentSeen") ?: false,
                                isOnline = document.getBoolean("isOnline") ?: false,
                                latestMessage = document.getString("latestMessage") ?: "",
                                unreadMessages = document.getLong("unreadMessages")?.toInt() ?: 0,
                                blinkNotesId = document.getString("blinkNotesId") ?: "",
                                isFriend = true, // Đánh dấu là bạn bè
                                following = following,
                                note = document.getString("note") ?: "",
                            )
                        } else {
                            null
                        }
                    }
                }
                _usersFriend.value = userList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }
    fun fetchUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userList = result.mapNotNull { document ->
                    if (document.id == currentUserId) {
                        null
                    } else {
                        val userId = document.id
                        val followingRaw = document.get("following") as? List<*> ?: emptyList<Any>()
                        val following = followingRaw.filterIsInstance<String>()
                        Log.d("FETCH_USERS", "User: $userId | Following: $following")
                        User(
                            userId = document.id,
                            username = document.getString("username") ?: "",
                            profileImage = document.getString("profileImage") ?: "",
                            hasMoment = document.getBoolean("hasMoment") ?: false,
                            isMomentSeen = document.getBoolean("isMomentSeen") ?: false,
                            isOnline = document.getBoolean("isOnline") ?: false,
                            latestMessage = document.getString("latestMessage") ?: "",
                            unreadMessages = document.getLong("unreadMessages")?.toInt() ?: 0,
                            blinkNotesId = document.getString("blinkNotesId") ?: "",
                            isFriend = document.getBoolean("isFriend") ?: false,
                            following = (document.get("following") as? List<*>
                                ?: emptyList<Any>()) as List<String>,
                            note = document.getString("note") ?: "",
                        )
                    }
                }
                _users.value = userList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }

    fun toggleActiveStatus() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val currentStatus = document.getBoolean("isOnline") ?: false
                val newStatus = !currentStatus

                firestore.collection("users")
                    .document(currentUserId)
                    .update("isOnline", newStatus)
                    .addOnSuccessListener {
                        _isActive.value = newStatus
                    }
            }
    }

    private val _friendStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val friendStatusMap: StateFlow<Map<String, Boolean>> = _friendStatusMap

    fun checkIfFriend(targetUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val following = document.get("following") as? List<String> ?: emptyList()
                val isFriend = following.contains(targetUserId)
                _friendStatusMap.value = _friendStatusMap.value.toMutableMap().apply {
                    this[targetUserId] = isFriend
                }
            }
            .addOnFailureListener {
                _friendStatusMap.value = _friendStatusMap.value.toMutableMap().apply {
                    this[targetUserId] = false
                }
            }
    }

    fun updateUserNote(note: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .document(currentUserId)
            .update("note", note)
            .addOnSuccessListener {
                Log.d("UPDATE_NOTE", "Note updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("UPDATE_NOTE", "Failed to update note", exception)
            }
    }
    fun sendMessage(
        senderId: String,
        receiverId: String,
        message: String,
        contentSendImage: String,
        imageUrls: List<String> = emptyList()
    ) {
        firestore.collection("users")
            .document(receiverId)
            .get()
            .addOnSuccessListener { receiverDocument ->
                val receiverToken = receiverDocument.getString("fcmToken")
                if (receiverToken != null) {
                    firestore.collection("users")
                        .document(senderId)
                        .get()
                        .addOnSuccessListener { senderDocument ->
                            val senderName = senderDocument.getString("username") ?: "Unknown"
                            val timestamp = System.currentTimeMillis()
                            val messageData = mapOf(
                                "senderId" to senderId,
                                "receiverId" to receiverId,
                                "content" to message,
                                "imageUrls" to imageUrls,
                                "timestamp" to timestamp,
                                "isRead" to false,
                                "contentSendImage" to contentSendImage,
                            )

                            FirebaseFirestore.getInstance().collection("contentchat")
                                .add(messageData)
                                .addOnSuccessListener {
                                    Log.d("SEND_MESSAGE", "Message sent successfully")
                                    val messageDto = SendMessageDto(
                                        to = receiverToken,
                                        notification = NotificationBody(
                                            title = "bạn có tin nhắn mới từ $senderName",
                                            body = if (message.isNotBlank()) message else contentSendImage
                                        )
                                    )

                                    viewModelScope.launch {
                                        try {
                                            api.sendMessage(messageDto)
                                            Log.d("SEND_MESSAGE", "FCM notification sent successfully")
                                        } catch (e: Exception) {
                                            Log.e("SEND_MESSAGE", "Failed to send FCM notification", e)
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("SEND_MESSAGE", "Failed to send message", exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("SEND_MESSAGE", "Failed to get sender username", exception)
                        }
                } else {
                    Log.e("SEND_MESSAGE", "Receiver FCM token not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SEND_MESSAGE", "Failed to get receiver FCM token", exception)
            }
    }

    fun listenForMessages(currentUserId: String, otherUserId: String) {
        _loadingMessages.value = true
        FirebaseFirestore.getInstance().collection("contentchat")
            .whereIn("senderId", listOf(currentUserId, otherUserId))
            .whereIn("receiverId", listOf(currentUserId, otherUserId))
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Messages", "Listen failed", error)
                    return@addSnapshotListener
                }

                val postPreviewMap = mutableMapOf<Long, Map<String, Any>>()
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val msg = doc.toObject(Message::class.java)?.copy(
                        imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                    )
                    if (msg != null && msg.content == "[shared_post]") {
                        val postPreview = doc.get("postPreview") as? Map<String, Any>
                        if (postPreview != null) {
                            postPreviewMap[msg.timestamp] = postPreview
                        }
                    }
                    msg
                } ?: emptyList()

                _messages.value = messages
                _postPreviews.value = postPreviewMap
                _loadingMessages.value = false
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
        val notification = SystemNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            type = type,
            createdAt = System.currentTimeMillis(),
            isRead = false,
            reporterId = reporterId,
            reporterName = reporterName,
            reporterImage = reporterImage,
            reportedId = reportedId,
            reportedName = reportedName,
            reportedImage = reportedImage,
            reportReason = reportReason
        )

        firestore.collection("system_notifications")
            .document(notification.id)
            .set(notification)
            .addOnSuccessListener {
                _latestSystemNotification.value = notification
            }
            .addOnFailureListener { e ->
                Log.e("SystemNotifications", "Error creating notification", e)
            }
    }
    private fun fetchSystemNotifications() {
        firestore.collection("system_notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SystemNotifications", "Listen failed", error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SystemNotification::class.java)
                } ?: emptyList()

                _systemNotifications.value = notifications
            }
    }
    private fun fetchUnreadSystemNotifications() {
        firestore.collection("system_notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { result ->
                val unreadMap = result.documents.associate { it.id to false }
                _unreadSystemNotifications.value = unreadMap
            }
    }
    private fun fetchLatestSystemNotification() {
        firestore.collection("system_notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                _latestSystemNotification.value = result.documents.firstOrNull()?.toObject(SystemNotification::class.java)
            }
    }
    private fun fetchUnreadActivityNotifications() {
        firestore.collection("activity_notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { result ->
                val unreadMap = result.documents.associate { it.id to false }
                _unreadActivityNotifications.value = unreadMap
            }
    }
    private fun fetchLatestActivityNotification() {
        firestore.collection("activity_notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                _latestActivityNotification.value = result.documents.firstOrNull()?.toObject(ActivityNotification::class.java)
            }
    }
    fun markSystemNotificationAsRead(notificationId: String) {
        firestore.collection("system_notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                _unreadSystemNotifications.value = _unreadSystemNotifications.value.toMutableMap().apply {
                    this[notificationId] = true
                }
            }
    }
    fun markActivityNotificationAsRead(notificationId: String) {
        firestore.collection("activity_notifications").document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                _unreadActivityNotifications.value = _unreadActivityNotifications.value.toMutableMap().apply {
                    this[notificationId] = true
                }
            }
    }

    fun reportUser(userId: String, reason: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { reporterDoc ->
                    val reporterName = reporterDoc.getString("username") ?: ""
                    val reporterImage = reporterDoc.getString("profileImage") ?: ""
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { reportedUserDoc ->
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
                            db.collection("users")
                                .whereEqualTo("isAdmin", true)
                                .get()
                                .addOnSuccessListener { adminDocs ->
                                    adminDocs.documents.forEach { adminDoc ->
                                        val adminToken = adminDoc.getString("fcmToken")
                                        if (!adminToken.isNullOrBlank()) {
                                            val messageDto = SendMessageDto(
                                                to = adminToken,
                                                notification = NotificationBody(
                                                    title = "Báo cáo người dùng mới",
                                                    body = "$reporterName đã báo cáo người dùng $reportedName"
                                                )
                                            )
                                            viewModelScope.launch {
                                                try {
                                                    api.sendMessage(messageDto)
                                                    Log.d("REPORT_NOTIFICATION", "FCM notification sent to admin successfully")
                                                } catch (e: Exception) {
                                                    Log.e("REPORT_NOTIFICATION", "Failed to send FCM notification to admin", e)
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                }
        }
    }
}
