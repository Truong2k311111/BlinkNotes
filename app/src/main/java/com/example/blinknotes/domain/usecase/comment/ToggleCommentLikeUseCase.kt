package com.example.blinknotes.domain.usecase.comment

import com.example.blinknotes.domain.model.NotificationType
import com.example.blinknotes.domain.repository.CommentRepository
import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class ToggleCommentLikeUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(commentId: String, userId: String) {
        val comment = commentRepository.getCommentById(commentId)
        val isLiked = commentRepository.checkCommentLikeStatus(commentId, userId)
        
        commentRepository.toggleCommentLike(commentId, userId)
        
        // Send notification if user likes the comment
        if (!isLiked && comment != null && comment.userId != userId) {
            val liker = userRepository.getUserById(userId)
            liker?.let { user ->
                notificationRepository.sendNotification(
                    type = NotificationType.LIKE_COMMENT.toString(),
                    title = "Bình luận của bạn được thích",
                    content = "${user.username} đã thích bình luận của bạn",
                    userId = userId,
                    postId = comment.postId,
                    commentId = commentId,
                    receiverId = comment.userId
                )
            }
        }
    }
} 