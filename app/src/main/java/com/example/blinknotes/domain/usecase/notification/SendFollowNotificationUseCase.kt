package com.example.blinknotes.domain.usecase.notification

import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class SendFollowNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(senderId: String, receiverId: String) {
        val sender = userRepository.getUserById(senderId)
        val receiver = userRepository.getUserById(receiverId)

        if (sender != null && receiver != null) {
            notificationRepository.sendNotification(
                type = "FOLLOWED_USER",
                title = "Bạn có người theo dõi mới",
                content = "${sender.username} đã theo dõi bạn.",
                userId = senderId,
                postId = null,
                commentId = null,
                receiverId = receiverId,
                imageUser = sender.profileImage
            )
        }
    }
} 