package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class TogglePostSaveUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, userId: String) {
        postRepository.togglePostSave(postId, userId)
    }
} 