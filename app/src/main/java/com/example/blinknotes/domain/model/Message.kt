package com.example.blinknotes.domain.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val contentSendImage: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
)