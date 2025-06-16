package com.example.blinknotes.domain.model

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