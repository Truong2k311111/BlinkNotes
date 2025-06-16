package com.example.blinknotes.domain.usecase.comment

import com.example.blinknotes.domain.repository.CommentRepository
import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        postId: String,
        userId: String,
        content: String,
        parentCommentId: String?
    ): Boolean {
        val success = commentRepository.addComment(postId, userId, content, parentCommentId)
        if (success) {
            // Extract tags and send notifications
            val tags = extractTags(content)
            val sender = userRepository.getUserById(userId)
            
            tags.forEach { tag ->
                val taggedUser = userRepository.getUserByBlinkNotesId(tag)
                taggedUser?.let { user ->
                    if (user.userId != userId) {
                        notificationRepository.sendTagNotification(
                            postId = postId,
                            senderId = userId,
                            content = content,
                            taggedUserId = user.userId
                        )
                    }
                }
            }
        }
        return success
    }

    private fun extractTags(content: String): List<String> {
        val regex = Regex("@(\\w+)")
        return regex.findAll(content).map { it.groupValues[1] }.toList()
    }
} 