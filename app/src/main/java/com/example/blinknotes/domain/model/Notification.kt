package com.example.blinknotes.domain.model

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val userId: String,
    val postId: String,
    val commentId: String,
    val timestamp: Long,
    val receiverId: String,
    val isRead: Boolean,
    val imageUser: String
) 