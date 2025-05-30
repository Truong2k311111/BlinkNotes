package com.example.blinknotes.ui.notify.notificationSysTem

import java.util.Date

data class SystemNotification(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val type: NotificationType = NotificationType.OTHER,
    val createdAt: Long = Date().time,
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
    USER_REPORTED
} 