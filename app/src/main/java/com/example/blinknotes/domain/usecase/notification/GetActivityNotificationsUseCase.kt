package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.model.ActivityNotification
import com.example.blinknotes.domain.repository.NotificationRepository
import javax.inject.Inject

class GetActivityNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): List<ActivityNotification> {
        return notificationRepository.getActivityNotifications(userId)
    }
} 