package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class CheckPostSaveStatusUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Boolean {
        return postRepository.checkPostSaveStatus(postId, userId)
    }
} 