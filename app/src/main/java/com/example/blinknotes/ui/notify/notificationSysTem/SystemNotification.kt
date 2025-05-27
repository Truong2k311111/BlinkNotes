package com.example.blinknotes.ui.notify.notificationSysTem

import java.util.Date

data class SystemNotification(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val type: NotificationType = NotificationType.OTHER,
    val createdAt: Long = Date().time,
    val isRead: Boolean = false,


)

enum class NotificationType {
    USER_BLOCKED,    // Thông báo chặn người dùng
    POST_REPORTED,   // Thông báo bài viết bị báo cáo
    SYSTEM_UPDATE,   // Thông báo cập nhật hệ thống
    MAINTENANCE,     // Thông báo bảo trì
    OTHER      ,
    USER_REPORTED

} 