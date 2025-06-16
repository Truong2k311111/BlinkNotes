package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkActivityNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Boolean {
        return notificationRepository.markActivityNotificationAsRead(notificationId)
    }
} 