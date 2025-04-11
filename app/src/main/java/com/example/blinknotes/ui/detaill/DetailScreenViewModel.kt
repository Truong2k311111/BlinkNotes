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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

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
//    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
//    val comments: StateFlow<List<Comment>> = _comments
    /**
     * Thêm comment vào Firestore và cập nhật danh sách comments.
     */
    fun addComment(postId: String, userId: String, content: String, parentCommentId: String? = null) {
        viewModelScope.launch {
            FirestoreHelper.addComment(postId, userId, content, parentCommentId) { success ->
                if (success) {
                    Log.d("DetailScreen", "Comment added successfully, re-fetching comments...")
                    getComments(postId)

                } else {
                    Log.e("DetailScreen", "Failed to add comment.")
                }
            }
        }
    }

    /**
     * Lấy danh sách comments của một bài viết.
     */
    fun getComments(postId: String) {
        Log.d("DetailScreen", "Fetching comments for post ID: $postId")

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

    /**
     * Lấy danh sách comments từ Firestore, sắp xếp theo thời gian.
     */
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

                // Gom nhóm các comments theo parentCommentId
                val groupedComments = allComments.groupBy { it.parentCommentId }
                val topLevelComments = groupedComments[null] ?: emptyList()

                // Hàm đệ quy để xây dựng cây comments
                fun buildCommentTree(comment: Comment): Comment {
                    val replies = groupedComments[comment.id] ?: emptyList()
                    return comment.copy(
                        replies = replies.map { buildCommentTree(it) }
                    )
                }

                // Xây dựng cây comments cho mỗi comment gốc
                val structuredComments = topLevelComments.map { buildCommentTree(it) }

                comments = structuredComments
                Log.d("DetailScreen", "Comments fetched successfully: ${comments.size} comments found.")
            }
            .addOnFailureListener { e ->
                Log.e("DetailScreen", "Error fetching comments: ${e.message}")
            }
    }

    /**
     * Lấy thông tin user từ Firestore.
     */
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

    // Kiểm tra trạng thái like của user cho một comment
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
                        // Cập nhật UI sau khi like/unlike thành công
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
                // Nếu hơn 1 tháng, hiển thị dạng "30 tháng ba"
                val sdf = SimpleDateFormat("dd 'tháng' MM", Locale("vi"))
                sdf.format(Date(timestamp))
            }
        }
    }

    // Lấy thông tin comment theo ID
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
                // Lấy thông tin người dùng hiện tại
                val currentUserDoc = db.collection("users").document(currentUserId).get().await()
                val currentUser = currentUserDoc.toObject(User::class.java)

                // Lấy thông tin người dùng mục tiêu
                val targetUserDoc = db.collection("users").document(targetUserId).get().await()
                val targetUser = targetUserDoc.toObject(User::class.java)

                if (currentUser != null && targetUser != null) {
                    // Kiểm tra xem người dùng hiện tại có follow người mục tiêu không
                    val isFollowing = currentUser.following.contains(targetUserId)
                    // Kiểm tra xem người mục tiêu có follow người dùng hiện tại không
                    val isFollowedBy = targetUser.following.contains(currentUserId)

                    // Lưu trạng thái với key là targetUserId
                    _followStatus.value = _followStatus.value + (targetUserId to FollowStatus(isFollowing, isFollowedBy))
                }
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error checking follow status", e)
            }
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

                    // Update Firestore
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

                    // Cập nhật lại trạng thái sau khi hoàn tất
                    val updatedCurrentUser = currentUserRef.get().await().toObject(User::class.java)
                    val updatedTargetUser = targetUserRef.get().await().toObject(User::class.java)

                    _followStatus.value = _followStatus.value + (targetUserId to FollowStatus(
                        isFollowing = updatedCurrentUser?.following?.contains(targetUserId) == true,
                        isFollowedBy = updatedTargetUser?.following?.contains(userId) == true
                    ))
                }
            } catch (e: Exception) {
                Log.e("DetailScreenViewModel", "Error toggling follow status", e)
            }
        }
    }
}
