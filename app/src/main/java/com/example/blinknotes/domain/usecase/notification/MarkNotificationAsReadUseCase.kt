package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.SystemNotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val systemNotificationRepository: SystemNotificationRepository
) {
    suspend operator fun invoke(notificationId: String, isSystemNotification: Boolean): Boolean {
        return if (isSystemNotification) {
            systemNotificationRepository.markSystemNotificationAsRead(notificationId)
        } else {
            notificationRepository.markActivityNotificationAsRead(notificationId)
        }
    }
} 