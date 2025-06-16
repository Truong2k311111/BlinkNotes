package com.example.blinknotes.domain.repository

import com.example.blinknotes.domain.model.Comment

interface CommentRepository {
    suspend fun addComment(postId: String, userId: String, content: String, parentCommentId: String?): Boolean
    suspend fun getComments(postId: String, lastTimestamp: Long? = null, pageSize: Int = 20): List<Comment>
    suspend fun toggleCommentLike(commentId: String, userId: String)
    suspend fun checkCommentLikeStatus(commentId: String, userId: String): Boolean
    suspend fun getCommentById(commentId: String): Comment?
    suspend fun deleteComment(commentId: String): Boolean
    suspend fun updateComment(commentId: String, content: String): Boolean
} 