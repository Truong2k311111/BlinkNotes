package com.example.blinknotes.data.model

data class SendMessageDto(
    val to: String,
    val notification: NotificationBody,
    val data: Map<String, String> = emptyMap()
)

data class NotificationBody(
    val title: String,
    val body: String,
    val click_action: String = "FLUTTER_NOTIFICATION_CLICK"
)