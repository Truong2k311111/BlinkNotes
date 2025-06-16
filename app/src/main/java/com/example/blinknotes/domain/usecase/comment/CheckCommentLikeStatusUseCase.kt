package com.example.blinknotes.domain.usecase.comment

import com.example.blinknotes.domain.repository.CommentRepository
import javax.inject.Inject

class CheckCommentLikeStatusUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(commentId: String, userId: String): Boolean {
        return commentRepository.checkCommentLikeStatus(commentId, userId)
    }
} 