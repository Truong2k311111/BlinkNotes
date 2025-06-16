package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.SystemNotification
import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.repository.PostRepository
import com.example.blinknotes.domain.repository.UserRepository
import com.example.blinknotes.domain.repository.NotificationRepository
import javax.inject.Inject

class ReportPostUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        postId: String,
        reporterId: String,
        reason: String
    ) {
        val post = postRepository.getPostById(postId)
        val reporter = userRepository.getUserById(reporterId)
        
        if (post != null && reporter != null) {
            val notification = SystemNotification(
                id = java.util.UUID.randomUUID().toString(),
                title = "Báo cáo bài viết",
                content = "Bài viết bị báo cáo với lý do: $reason",
                type = NotificationType.POST_REPORTED,
                createdAt = System.currentTimeMillis(),
                isRead = false,
                reporterId = reporterId,
                reporterName = reporter.username,
                reporterImage = reporter.profileImage,
                reportedId = postId,
                reportedName = post.caption,
                reportedImage = post.imageUrls.firstOrNull() ?: "",
                reportReason = reason
            )

            // Get all admin users
            val adminUsers = userRepository.getAllUsers().filter { it.isAdmin }
            adminUsers.forEach { admin ->
                notificationRepository.sendNotification(
                    type = NotificationType.POST_REPORTED.toString(),
                    title = "Báo cáo bài viết mới",
                    content = "${reporter.username} đã báo cáo bài viết của ${post.caption}",
                    userId = reporterId,
                    postId = postId,
                    commentId = null,
                    receiverId = admin.userId
                )
            }
        }
    }
} 