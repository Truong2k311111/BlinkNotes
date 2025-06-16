package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import com.example.blinknotes.domain.repository.NotificationRepository
import javax.inject.Inject

class SharePostUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        post: Post,
        senderId: String,
        receiverId: String
    ) {
        // Share post in chat
        postRepository.sharePostInChat(
            postId = post.id,
            senderId = senderId,
            receiverId = receiverId,
            imageUrl = post.imageUrls.firstOrNull(),
            caption = post.caption,
            content = post.content
        )

        // Send notification to receiver
        notificationRepository.sendNotification(
            type = "SHARED_POST",
            title = "Bạn nhận được một bài viết được chia sẻ",
            content = "Bạn vừa nhận được một bài viết được chia sẻ từ người khác.",
            userId = senderId,
            postId = post.id,
            commentId = null,
            receiverId = receiverId
        )
    }
} 