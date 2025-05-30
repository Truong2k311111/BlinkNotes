package com.example.blinknotes.ui.detaill

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.data.helper.FirestoreHelper
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.User
import com.example.blinknotes.ui.notify.FcmApi
import com.example.blinknotes.ui.notify.NotificationBody
import com.example.blinknotes.ui.notify.SendMessageDto
import com.example.blinknotes.ui.notify.notificationSysTem.NotificationType
import com.example.blinknotes.ui.notify.notificationSysTem.SystemNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val parentCommentId: String? = null,
    val replies: List<Comment> = emptyList(),
    val isAuthor: Boolean = false
)

class DetailScreenViewModel : ViewModel() {
    var comments by mutableStateOf<List<Comment>>(emptyList())
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("https://blinknotes-api.onrender.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    fun addComment(postId: String, userId: String, content: String, parentCommentId: String? = null) {
        viewModelScope.launch {
            FirestoreHelper.addComment(postId, userId, content, parentCommentId) { success ->
                if (success) {
                    getComments(postId)
                    sendTagNotificationsIfNeeded(postId, userId, content, api = api, coroutineScope = viewModelScope)                } else {
                }
            }
        }
    }
    private fun sendTagNotificationsIfNeeded(
        postId: String,
        senderId: String,
        content: String,
        api: FcmApi,
        coroutineScope: CoroutineScope
    ) {
        val regex = Regex("@(\\w+)")
        val tags = regex.findAll(content).map { it.groupValues[1] }.toSet()
        if (tags.isEmpty()) return

        val db = FirebaseFirestore.getInstance()
        val senderRef = db.collection("users").document(senderId)

        senderRef.get().addOnSuccessListener { senderDoc ->
            val senderName = senderDoc.getString("username") ?: "Người dùng"
            val imageUser = senderDoc.getString("profileImage") ?: ""

            tags.forEach { blinkNotesId ->
                db.collection("users")
                    .whereEqualTo("blinkNotesId", "@$blinkNotesId")
                    .get()
                    .addOnSuccessListener { result ->
                        val taggedUserDoc = result.documents.firstOrNull()
                        val taggedUserId = taggedUserDoc?.id
                        val receiverToken = taggedUserDoc?.getString("fcmToken")

                        if (taggedUserId != null && taggedUserId != senderId) {
                            val notificationId = UUID.randomUUID().toString()
                            val notification = mapOf(
                                "id" to notificationId,
                                "type" to "TAGGED_IN_COMMENT",
                                "title" to "Bạn được tag trong bình luận",
                                "content" to "$senderName đã tag bạn trong một bình luận.",
                                "userId" to senderId,
                                "postId" to postId,
                                "commentId" to "",
                                "timestamp" to System.currentTimeMillis(),
                                "receiverId" to taggedUserId,
                                "isRead" to false,
                                "imageUser" to imageUser
                            )

                            db.collection("activity_notifications")
                                .document(notificationId)
                                .set(notification)
                            if (!receiverToken.isNullOrBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val messageDto = SendMessageDto(
                                            to = receiverToken,
                                            notification = NotificationBody(
                                                title = "Bạn được tag trong bình luận",
                                                body = "$senderName đã tag bạn trong một bình luận."
                                            )
                                        )
                                        api.sendMessage(messageDto)
                                        Log.d("TAG_NOTIFICATION", "FCM notification sent successfully")
                                    } catch (e: Exception) {
                                        Log.e("TAG_NOTIFICATION", "Failed to send FCM notification", e)
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
    fun getComments(postId: String) {
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { postSnapshot ->
                val postAuthorId = postSnapshot.getString("userId")
                if (postAuthorId != null) {
                    fetchComments(postId, postAuthorId)
                } else {
                    Log.e("DetailScreen", "Post not found or missing userId for ID: $postId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreen", "Error fetching post details: ${e.message}")
            }
    }
    fun fetchComments(postId: String, postAuthorId: String) {
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val allComments = result.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Comment(
                        id = doc.id,
                        postId = data["postId"] as String,
                        userId = data["userId"] as String,
                        content = data["content"] as String,
                        createdAt = data["createdAt"] as Long,
                        likes = data["likes"] as? List<String> ?: emptyList(),
                        parentCommentId = data["parentCommentId"] as? String,
                        isAuthor = (data["userId"] as String) == postAuthorId
                    )
                }
                val groupedComments = allComments.groupBy { it.parentCommentId }
                val topLevelComments = groupedComments[null] ?: emptyList()

                fun buildCommentTree(comment: Comment): Comment {
                    val replies = groupedComments[comment.id] ?: emptyList()
                    return comment.copy(
                        replies = replies.map { buildCommentTree(it) }
                    )
                }
                val structuredComments = topLevelComments.map { buildCommentTree(it) }

                comments = structuredComments
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreen", "Error fetching comments: ${e.message}")
            }
    }
    fun getUserById(userId: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)?.copy(userId = document.id)
                        callback(user)
                        Log.d("DetailScreen", "User fetched: $user")
                    } else {
                        callback(null)
                        Log.e("DetailScreen", "User not found for ID: $userId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DetailScreen", "Error fetching user: ${e.message}")
                    callback(null)
                }
        }
    }
    fun getUserByBlinkNotesId(blinkNotesId: String, callback: (User?) -> Unit) {
        db.collection("users")
            .whereEqualTo("blinkNotesId", "@$blinkNotesId")
            .get()
            .addOnSuccessListener { result ->
                val user = result.documents.firstOrNull()?.toObject(User::class.java)?.copy(userId = result.documents.firstOrNull()?.id ?: "")
                callback(user)
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreenViewModel", "Error fetching user by blinkNotesId: ${e.message}")
                callback(null)
            }
    }
    fun checkCommentLikeStatus(commentId: String, userId: String, callback: (Boolean) -> Unit) {
        db.collection("comments").document(commentId)
            .get()
            .addOnSuccessListener { document ->
                val likes = document.get("likes") as? List<String> ?: emptyList()
                callback(userId in likes)
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreen", "Error checking like status: ${e.message}")
                callback(false)
            }
    }

    // Toggle like cho comment
    fun toggleCommentLike(commentId: String, userId: String) {
        db.collection("comments").document(commentId)
            .get()
            .addOnSuccessListener { document ->
                val likes = document.get("likes") as? List<String> ?: emptyList()
                val newLikes = if (userId in likes) {
                    likes - userId
                } else {
                    likes + userId
                }
                db.collection("comments").document(commentId)
                    .update("likes", newLikes)
                    .addOnSuccessListener {
                        comments = comments.map { comment ->
                            if (comment.id == commentId) {
                                comment.copy(likes = newLikes)
                            } else {
                                comment
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DetailScreen", "Error updating likes: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreen", "Error getting comment: ${e.message}")
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
        db.collection("comments")
            .document(commentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val comment = Comment(
                            id = document.id,
                            postId = data["postId"] as String,
                            userId = data["userId"] as String,
                            content = data["content"] as String,
                            createdAt = data["createdAt"] as Long,
                            likes = data["likes"] as? List<String> ?: emptyList(),
                            parentCommentId = data["parentCommentId"] as? String,
                            isAuthor = false
                        )
                        callback(comment)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreen", "Error fetching comment: ${e.message}")
                callback(null)
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
        val chunks = userIds.chunked(10)
        val allPosts = mutableListOf<Post>()

        val totalChunks = chunks.size
        var completed = 0

        for (chunk in chunks) {
            db.collection("posts")
                .whereIn("userId", chunk)
                .get()
                .addOnSuccessListener { result ->
                    val posts = result.documents.mapNotNull { doc ->
                        val data = doc.data
                        if (data != null) {
                            Post(
                                id = doc.id,
                                userId = data["userId"] as? String ?: "",
                                userIdCmt = data["userIdCmt"] as? String ?: "",
                                imageUrls = data["imageUrls"] as? List<String> ?: emptyList(),
                                firstImageUrl = (data["imageUrls"] as? List<String>)?.firstOrNull() ?: "",
                                caption = data["caption"] as? String ?: "",
                                content = data["content"] as? String ?: "",
                                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                                likesCount = (data["likesCount"] as? Long)?.toInt() ?: 0,
                                commentsCount = (data["commentsCount"] as? Long)?.toInt() ?: 0,
                                visibility = data["visibility"] as? String ?: "public",
                                tags = data["tags"] as? List<String> ?: emptyList()
                            )
                        } else null
                    }
                    allPosts += posts
                    completed++
                    if (completed == totalChunks) {
                        callback(allPosts)
                    }
                }
                .addOnFailureListener {
                    completed++
                    if (completed == totalChunks) {
                        callback(allPosts)
                    }
                }
        }
    }

    fun checkFollowStatus(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserDoc = db.collection("users").document(currentUserId).get().await()
                val currentUser = currentUserDoc.toObject(User::class.java)
                val targetUserDoc = db.collection("users").document(targetUserId).get().await()
                val targetUser = targetUserDoc.toObject(User::class.java)

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
    private fun callApiSendFollowNotification(
        senderId: String,
        receiverId: String,
        senderName: String,
        api: FcmApi
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(receiverId)
            .get()
            .addOnSuccessListener { receiverDocument ->
                val receiverToken = receiverDocument.getString("fcmToken")
                if (receiverToken != null) {
                    val messageDto = SendMessageDto(
                        to = receiverToken,
                        notification = NotificationBody(
                            title = "Bạn có người theo dõi mới",
                            body = "$senderName đã theo dõi bạn."
                        )
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            api.sendMessage(messageDto)
                            Log.d("FCM_FOLLOW", "Gửi thông báo follow thành công")
                        } catch (e: Exception) {
                            Log.e("FCM_FOLLOW", "Gửi thông báo follow thất bại", e)
                        }
                    }
                } else {
                    Log.e("FCM_FOLLOW", "FCM token không tồn tại cho người nhận")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM_FOLLOW", "Không thể lấy thông tin người nhận", e)
            }
    }
    fun toggleFollow(userId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserRef = db.collection("users").document(userId)
                val targetUserRef = db.collection("users").document(targetUserId)

                val currentUserDoc = currentUserRef.get().await()
                val targetUserDoc = targetUserRef.get().await()

                val currentUser = currentUserDoc.toObject(User::class.java)
                val targetUser = targetUserDoc.toObject(User::class.java)

                if (currentUser != null && targetUser != null) {
                    val isCurrentlyFollowing = currentUser.following.contains(targetUserId)
                    currentUserRef.update(
                        "following",
                        if (isCurrentlyFollowing) FieldValue.arrayRemove(targetUserId)
                        else FieldValue.arrayUnion(targetUserId)
                    )

                    targetUserRef.update(
                        "followers",
                        if (isCurrentlyFollowing) FieldValue.arrayRemove(userId)
                        else FieldValue.arrayUnion(userId)
                    )
                    val updatedCurrentUser = currentUserRef.get().await().toObject(User::class.java)
                    val updatedTargetUser = targetUserRef.get().await().toObject(User::class.java)
                    _followStatus.value = _followStatus.value + (targetUserId to FollowStatus(
                        isFollowing = updatedCurrentUser?.following?.contains(targetUserId) == true,
                        isFollowedBy = updatedTargetUser?.following?.contains(userId) == true
                    ))
                    if (!isCurrentlyFollowing) {
                        try {
                            val notificationId = UUID.randomUUID().toString()
                            val senderName = currentUser.username
                            val senderImage = currentUser.profileImage
                            val notification = mapOf(
                                "id" to notificationId,
                                "type" to "FOLLOWED_USER",
                                "title" to "Bạn có người theo dõi mới",
                                "content" to "$senderName đã theo dõi bạn.",
                                "userId" to userId,
                                "postId" to null,
                                "commentId" to null,
                                "timestamp" to System.currentTimeMillis(),
                                "receiverId" to targetUserId,
                                "isRead" to false,
                                "imageUser" to senderImage
                            )
                            db.collection("activity_notifications")
                                .document(notificationId)
                                .set(notification)

                            callApiSendFollowNotification(
                                senderId = userId,
                                receiverId = targetUserId,
                                senderName = senderName,
                                api = api
                            )
                        } catch (e: Exception) {
                            Log.e("DetailScreenViewModel", "Error sending follow notification", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error toggling follow status", e)
            }
        }
    }

    fun reportPost(postId: String, reason: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { reporterDoc ->
                    val reporterName = reporterDoc.getString("username") ?: ""
                    val reporterImage = reporterDoc.getString("profileImage") ?: ""
                    db.collection("posts").document(postId)
                        .get()
                        .addOnSuccessListener { postDoc ->
                            val postAuthorId = postDoc.getString("userId") ?: ""
                            val postCaption = postDoc.getString("caption") ?: ""
                            val postImage = (postDoc.get("imageUrls") as? List<String>)?.firstOrNull() ?: ""
                            db.collection("users").document(postAuthorId)
                                .get()
                                .addOnSuccessListener { authorDoc ->
                                    val authorName = authorDoc.getString("username") ?: ""
                                    val authorImage = authorDoc.getString("profileImage") ?: ""
                                    createSystemNotification(
                                        type = NotificationType.POST_REPORTED,
                                        title = "Báo cáo bài viết",
                                        content = "Bài viết bị báo cáo với lý do: $reason",
                                        reporterId = currentUser.uid,
                                        reporterName = reporterName,
                                        reporterImage = reporterImage,
                                        reportedId = postId,
                                        reportedName = postCaption,
                                        reportedImage = postImage,
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
                                                            title = "Báo cáo bài viết mới",
                                                            body = "$reporterName đã báo cáo bài viết của $authorName"
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

        val db = FirebaseFirestore.getInstance()
        db.collection("system_notifications")
            .document(notification.id)
            .set(notification)
            .addOnSuccessListener {
                Log.d("SYSTEM_NOTIFICATION", "Notification created successfully")
            }
            .addOnFailureListener { e ->
                Log.e("SYSTEM_NOTIFICATION", "Error creating notification", e)
            }
    }
    fun updateComment(commentId: String, newContent: String) {
        db.collection("comments").document(commentId)
            .update("content", newContent)
            .addOnSuccessListener {
                comments = comments.map { comment ->
                    if (comment.id == commentId) comment.copy(content = newContent) else comment
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreenViewModel", "Error updating comment: ${e.message}")
            }
    }
    fun deleteComment(commentId: String) {
        db.collection("comments").document(commentId)
            .delete()
            .addOnSuccessListener {
                fun removeCommentRecursive(list: List<Comment>): List<Comment> {
                    return list.filter { it.id != commentId }.map { comment ->
                        comment.copy(replies = removeCommentRecursive(comment.replies))
                    }
                }
                comments = removeCommentRecursive(comments)
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreenViewModel", "Error deleting comment: ${e.message}")
            }
    }
    fun sharePostWithUser(
        post: Post,
        senderId: String,
        receiverId: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val db = FirebaseFirestore.getInstance()
        val postPreview = mapOf(
            "postId" to post.id,
            "imageUrls" to post.imageUrls.firstOrNull(),
            "caption" to post.caption,
            "content" to post.content,
            "sharedAt" to System.currentTimeMillis()
        )
        val messageData = mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to "[shared_post]",
            "postPreview" to postPreview,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false,
            "contentSendImage" to "",
            "imageUrls" to emptyList<String>()
        )
        db.collection("contentchat")
            .add(messageData)
            .addOnSuccessListener {
                onSuccess?.invoke()
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(receiverId)
                    .get()
                    .addOnSuccessListener { receiverDoc ->
                        val receiverToken = receiverDoc.getString("fcmToken")
                        if (!receiverToken.isNullOrBlank()) {
                            val messageDto = SendMessageDto(
                                to = receiverToken,
                                notification = NotificationBody(
                                    title = "Bạn nhận được một bài viết được chia sẻ",
                                    body = "Bạn vừa nhận được một bài viết được chia sẻ từ người khác."
                                )
                            )
                            viewModelScope.launch {
                                try {
                                    api.sendMessage(messageDto)
                                    Log.d("SHARE_POST_NOTIFICATION", "FCM notification sent to receiver successfully")
                                } catch (e: Exception) {
                                    Log.e("SHARE_POST_NOTIFICATION", "Failed to send FCM notification to receiver", e)
                                }
                            }
                        }
                    }
            }
            .addOnFailureListener { e -> onFailure?.invoke(e) }
    }
}
