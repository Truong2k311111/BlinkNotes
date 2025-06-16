package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.repository.SystemNotificationRepository
import javax.inject.Inject

class DeleteSystemNotificationUseCase @Inject constructor(
    private val systemNotificationRepository: SystemNotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Boolean {
        return systemNotificationRepository.deleteSystemNotification(notificationId)
    }
} 