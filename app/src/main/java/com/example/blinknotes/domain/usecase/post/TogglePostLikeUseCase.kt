package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.repository.PostRepository
import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.UserRepository
import com.example.blinknotes.domain.model.NotificationType
import javax.inject.Inject

class TogglePostLikeUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(postId: String, userId: String) {
        val post = postRepository.getPostById(postId)
        val isLiked = postRepository.checkPostLikeStatus(postId, userId)
        
        postRepository.togglePostLike(postId, userId)
        
        // Send notification if user likes the post
        if (!isLiked && post != null && post.userId != userId) {
            val liker = userRepository.getUserById(userId)
            liker?.let { user ->
                notificationRepository.sendNotification(
                    type = NotificationType.LIKE_POST.toString(),
                    title = "Bài viết của bạn được thích",
                    content = "${user.username} đã thích bài viết của bạn",
                    userId = userId,
                    postId = postId,
                    commentId = null,
                    receiverId = post.userId
                )
            }
        }
    }
} 