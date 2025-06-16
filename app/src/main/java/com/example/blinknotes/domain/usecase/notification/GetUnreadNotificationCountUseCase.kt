package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.SystemNotificationRepository
import javax.inject.Inject

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val systemNotificationRepository: SystemNotificationRepository
) {
    suspend operator fun invoke(userId: String): Pair<Int, Int> {
        val activityCount = notificationRepository.getUnreadNotificationCount(userId)
        val systemCount = systemNotificationRepository.getUnreadSystemNotificationCount()
        return Pair(activityCount, systemCount)
    }
} 