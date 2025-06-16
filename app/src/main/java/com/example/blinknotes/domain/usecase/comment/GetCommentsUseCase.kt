package com.example.blinknotes.domain.usecase.comment

import com.example.blinknotes.domain.model.Comment
import com.example.blinknotes.domain.repository.CommentRepository
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(
        postId: String,
        lastTimestamp: Long? = null,
        pageSize: Int = 20
    ): List<Comment> {
        val comments = commentRepository.getComments(postId, lastTimestamp, pageSize)
        return buildCommentTree(comments)
    }

    private fun buildCommentTree(comments: List<Comment>): List<Comment> {
        val groupedComments = comments.groupBy { it.parentCommentId }
        val topLevelComments = groupedComments[null] ?: emptyList()

        fun buildCommentTreeRecursive(comment: Comment): Comment {
            val replies = groupedComments[comment.id] ?: emptyList()
            return comment.copy(
                replies = replies.map { buildCommentTreeRecursive(it) }
            )
        }

        return topLevelComments.map { buildCommentTreeRecursive(it) }
    }
} 