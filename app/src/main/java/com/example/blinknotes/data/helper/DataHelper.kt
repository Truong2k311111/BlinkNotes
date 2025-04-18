package com.example.blinknotes.data.helper

import android.util.Log
import com.example.blinknotes.ui.detaill.Comment
import com.example.blinknotes.ui.home.Post
import com.example.blinknotes.ui.home.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

object FirestoreHelper {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // 1. Thêm người dùng vào Firestore
    fun addUser(userId: String, username: String, email: String, profileImage: String, callback: (Boolean, String) -> Unit) {
        val user = mapOf(
            "username" to username,
            "email" to email,
            "profileImage" to profileImage,
            "followers" to emptyList<String>(),
            "following" to emptyList<String>(),
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener { callback(true, "Registration Successful") }
            .addOnFailureListener { e -> callback(false, "Error saving profile: ${e.localizedMessage}") }
    }

    // 2. Đăng bài viết
    fun addPost(postId: String, userId: String, imageUrl: String, caption: String, content: String) {
        val post = mapOf(
            "userId" to userId,
            "imageUrl" to imageUrl,
            "caption" to caption,
            "content" to content,
            "createdAt" to System.currentTimeMillis(),
            "likesCount" to 0,
            "commentsCount" to 0,
            "visibility" to "public",
            "tags" to listOf("travel", "food")
        )
        db.collection("posts").document(postId)
            .set(post)
            .addOnFailureListener { e -> Log.e("Firestore", "Error adding post: $e") }
    }

    // 3. Cập nhật bài viết
    fun updatePost(postId: String, caption: String, visibility: String) {
        db.collection("posts").document(postId)
            .update(mapOf("caption" to caption, "visibility" to visibility))
            .addOnFailureListener { e -> Log.e("Firestore", "Error updating post: $e") }
    }

    // 4. Xóa bài viết
    fun deletePost(postId: String) {
        db.collection("posts").document(postId)
            .delete()
            .addOnFailureListener { e -> Log.e("Firestore", "Error deleting post: $e") }
    }

    // 5. Lấy danh sách bài viết
    fun getAllPosts(lastPost: Post? = null, callback: (List<Post>) -> Unit) {
        // Lấy tất cả bài viết có visibility là "public"
        FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("visibility", "public")
            .whereEqualTo( "status", "active")
            .get()
            .addOnSuccessListener { result ->
                val postsList = result.documents.mapNotNull { doc ->
                    val id = doc.id
                    val userId = doc.getString("userId") ?: ""
                    val userIdCmt = doc.getString("userIdCmt") ?: ""
                    val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                    val firstImageUrl = imageUrls.firstOrNull() ?: ""
                    val caption = doc.getString("caption") ?: ""
                    val content = doc.getString("content") ?: ""
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                    val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                    val visibility = doc.getString("visibility") ?: "public"
                    val tags = doc.get("tags") as? List<String> ?: emptyList()
                    val status = doc.getString("status") ?: "draft"

                    Post(id, userId, userIdCmt, imageUrls, firstImageUrl, caption, content, createdAt, likesCount, commentsCount, visibility, tags, status)
                }

                // Xáo trộn danh sách bài viết
                val shuffledPosts = postsList.shuffled()
                
                // Nếu có lastPost, lọc ra các bài viết đã hiển thị
                val filteredPosts = if (lastPost != null) {
                    shuffledPosts.filter { it.id != lastPost.id }
                } else {
                    shuffledPosts
                }

                // Lấy 10 bài viết đầu tiên sau khi xáo trộn
                callback(filteredPosts.take(10))
            }
            .addOnFailureListener { e ->
                callback(emptyList())
                Log.e("Firestore", "Lỗi khi tải dữ liệu: ${e.message}")
            }
    }

    // 6. Thêm bình luận
    fun addComment(postId: String, userId: String, content: String, parentCommentId: String? = null,  onComplete: (Boolean) -> Unit) {
        val comment = hashMapOf(
            "postId" to postId,
            "userId" to userId,
            "content" to content,
            "createdAt" to System.currentTimeMillis(),
            "likesCount" to 0,
            "parentCommentId" to parentCommentId // Nếu là trả lời, lưu ID của comment cha
        )


        db.collection("comments").add(comment)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Comment added with ID: ${documentReference.id}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding comment: $e")
                onComplete(false)
            }
    }

    // 7. Lấy bình luận
//    fun getComments(postId: String, onResult: (List<Comment>) -> Unit) {
//        db.collection("comments")
//            .whereEqualTo("postId", postId)
//            .orderBy("createdAt", Query.Direction.DESCENDING) // Sắp xếp bình luận mới nhất lên trên
//            .get()
//            .addOnSuccessListener { result ->
//                val comments = result.documents.mapNotNull { document ->
//                    val data = document.data
//                    data?.let {
//                        Comment(
//                            id = document.id,
//                            postId = it["postId"] as String,
//                            userId = it["userId"] as String,
//                            content = it["content"] as String,
//                            createdAt = it["createdAt"] as Long,
//                            likesCount = it["likes"] as? Int ?: emptyList(),
//                            parentCommentId = it["parentCommentId"] as? String
//                        )
//                    }
//                }
//                val groupedComments = comments.groupBy { it.parentCommentId }
//                val topLevelComments = groupedComments[null] ?: emptyList()
//
//                val structuredComments = topLevelComments.map { parentComment ->
//                    parentComment.copy(
//                        replies = groupedComments[parentComment.id] ?: emptyList()
//                    )
//                }
//
//                onResult(structuredComments)
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Error getting comments: $e")
//                onResult(emptyList())
//            }
//    }


    // 8. Thích bài viết
    fun likePost(postId: String, userId: String) {
        db.collection("likes").add(mapOf("postId" to postId, "userId" to userId, "createdAt" to System.currentTimeMillis()))
            .addOnFailureListener { e -> Log.e("Firestore", "Error liking post: $e") }
    }

    // 9. Hủy thích bài viết
    fun unlikePost(postId: String, userId: String) {
        db.collection("likes").whereEqualTo("postId", postId).whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                result.documents.forEach { doc ->
                    db.collection("likes").document(doc.id).delete()
                        .addOnFailureListener { e -> Log.e("Firestore", "Error unliking post: $e") }
                }
            }
            .addOnFailureListener { e -> Log.e("Firestore", "Error fetching likes: $e") }
    }

    // 10. Lấy số lượng lượt thích
    fun getLikesCount(postId: String, onResult: (Int) -> Unit) {
        db.collection("likes").whereEqualTo("postId", postId).get()
            .addOnSuccessListener { result -> onResult(result.size()) }
            .addOnFailureListener { e -> Log.e("Firestore", "Error getting likes count: $e") }
    }

    // 11. Quản lý theo dõi
    fun updateFollowers(userId: String, followerId: String, add: Boolean) {
        val fieldOp = if (add) FieldValue.arrayUnion(followerId) else FieldValue.arrayRemove(followerId)
        db.collection("users").document(userId)
            .update("followers", fieldOp)
            .addOnFailureListener { e -> Log.e("Firestore", "Error updating followers: $e") }
    }

    // 12. Tìm kiếm bài viết theo thẻ tag
    fun searchPostsByTag(tag: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("posts").whereArrayContains("tags", tag)
            .get()
            .addOnSuccessListener { result -> onResult(result.documents.mapNotNull { it.data }) }
            .addOnFailureListener { e -> Log.e("Firestore", "Error searching posts: $e") }
    }

    // 13. Lấy thông tin người dùng
    fun getUser(userId: String, onResult: (User?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)?.copy(userId = document.id)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { e -> Log.e("Firestore", "Error getting user: ${e.message}") }
    }
}
