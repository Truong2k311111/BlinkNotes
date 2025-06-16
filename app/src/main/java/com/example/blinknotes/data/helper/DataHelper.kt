package com.example.blinknotes.data.helper

import android.util.Log
import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
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
    fun getAllPosts2( lastPost: Post? = null, callback: (List<Post>) -> Unit) {
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
                val shuffledPosts = postsList.shuffled()
                val filteredPosts = if (lastPost != null) {
                    shuffledPosts.filter { it.id != lastPost.id }
                } else {
                    shuffledPosts
                }
                callback(filteredPosts.take(10))
            }
            .addOnFailureListener { e ->
                callback(emptyList())
                Log.e("Firestore", "Lỗi khi tải dữ liệu: ${e.message}")
            }
    }
    fun addComment(postId: String, userId: String, content: String, parentCommentId: String? = null,  onComplete: (Boolean) -> Unit) {
        val comment = hashMapOf(
            "postId" to postId,
            "userId" to userId,
            "content" to content,
            "createdAt" to System.currentTimeMillis(),
            "likesCount" to 0,
            "parentCommentId" to parentCommentId
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
