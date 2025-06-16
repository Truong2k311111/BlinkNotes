package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class SaveDraftUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(caption: String, content: String, visibility: String, imageUris: List<String>): Post {
        return postRepository.saveDraft(caption, content, visibility, imageUris)
    }
} 