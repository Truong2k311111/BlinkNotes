package com.example.blinknotes.domain.model

data class ActivityNotification(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val content: String = "",
    val userId: String? = null,
    val postId: String? = null,
    val commentId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUser : String? = null
)