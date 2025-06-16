package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.model.SystemNotification
import com.example.blinknotes.domain.repository.SystemNotificationRepository
import javax.inject.Inject

class CreateSystemNotificationUseCase @Inject constructor(
    private val systemNotificationRepository: SystemNotificationRepository
) {
    suspend operator fun invoke(
        type: NotificationType,
        title: String,
        content: String,
        reporterId: String = "",
        reporterName: String = "",
        reporterImage: String = "",
        reportedId: String = "",
        reportedName: String = "",
        reportedImage: String = "",
        reportReason: String = ""
    ) {
        val notification = SystemNotification(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            content = content,
            type = type,
            createdAt = System.currentTimeMillis(),
            isRead = false,
            reporterId = reporterId,
            reporterName = reporterName,
            reporterImage = reporterImage,
            reportedId = reportedId,
            reportedName = reportedName,
            reportedImage = reportedImage,
            reportReason = reportReason
        )
        systemNotificationRepository.createSystemNotification(notification)
    }
} 