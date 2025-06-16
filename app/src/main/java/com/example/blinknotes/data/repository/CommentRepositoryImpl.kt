package com.example.blinknotes.data.repository

import com.example.blinknotes.domain.model.Comment
import com.example.blinknotes.domain.repository.CommentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : CommentRepository {

    override suspend fun addComment(postId: String, userId: String, content: String, parentCommentId: String?): Boolean {
        return try {
            val commentData = hashMapOf(
                "postId" to postId,
                "userId" to userId,
                "content" to content,
                "createdAt" to System.currentTimeMillis(),
                "likes" to emptyList<String>(),
                "parentCommentId" to parentCommentId
            )
            db.collection("comments").add(commentData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getComments(postId: String, lastTimestamp: Long?, pageSize: Int): List<Comment> {
        val query = db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(pageSize.toLong())

        val result = if (lastTimestamp != null) {
            query.startAfter(lastTimestamp)
                .get()
                .await()
        } else {
            query.get().await()
        }
        
        return result.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            Comment(
                id = doc.id,
                postId = data["postId"] as String,
                userId = data["userId"] as String,
                content = data["content"] as String,
                createdAt = data["createdAt"] as Long,
                likes = data["likes"] as? List<String> ?: emptyList(),
                parentCommentId = data["parentCommentId"] as? String,
                isAuthor = false
            )
        }
    }

    override suspend fun toggleCommentLike(commentId: String, userId: String) {
        val commentRef = db.collection("comments").document(commentId)
        val comment = commentRef.get().await()
        val likes = comment.get("likes") as? List<String> ?: emptyList()
        
        val newLikes = if (userId in likes) {
            likes - userId
        } else {
            likes + userId
        }
        
        commentRef.update("likes", newLikes).await()
    }

    override suspend fun checkCommentLikeStatus(commentId: String, userId: String): Boolean {
        val comment = db.collection("comments").document(commentId).get().await()
        val likes = comment.get("likes") as? List<String> ?: emptyList()
        return userId in likes
    }

    override suspend fun getCommentById(commentId: String): Comment? {
        return try {
            val doc = db.collection("comments").document(commentId).get().await()
            val data = doc.data ?: return null
            Comment(
                id = doc.id,
                postId = data["postId"] as String,
                userId = data["userId"] as String,
                content = data["content"] as String,
                createdAt = data["createdAt"] as Long,
                likes = data["likes"] as? List<String> ?: emptyList(),
                parentCommentId = data["parentCommentId"] as? String,
                isAuthor = false
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteComment(commentId: String): Boolean {
        return try {
            db.collection("comments").document(commentId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateComment(commentId: String, content: String): Boolean {
        return try {
            db.collection("comments").document(commentId)
                .update("content", content)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
} 