package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.model.SystemNotification
import com.example.blinknotes.domain.repository.SystemNotificationRepository
import javax.inject.Inject

class GetSystemNotificationsUseCase @Inject constructor(
    private val systemNotificationRepository: SystemNotificationRepository
) {
    suspend operator fun invoke(): List<SystemNotification> {
        return systemNotificationRepository.getSystemNotifications()
    }
} 