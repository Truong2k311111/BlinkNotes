package com.example.blinknotes.domain.model

data class SystemNotification(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val type: NotificationType = NotificationType.OTHER,
    val createdAt: Long = 0L,
    val isRead: Boolean = false,
    val reporterId: String = "",
    val reporterName: String = "",
    val reporterImage: String = "",
    val reportedId: String = "",
    val reportedName: String = "",
    val reportedImage: String = "",
    val reportReason: String = ""
)

enum class NotificationType {
    USER_BLOCKED,
    POST_REPORTED,
    SYSTEM_UPDATE,
    MAINTENANCE,
    OTHER,
    USER_REPORTED,
    LIKE_POST,
    LIKE_COMMENT,
    TAGGED_IN_COMMENT
} 