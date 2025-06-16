package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class LoadDraftUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(draftId: String): Post? {
        return postRepository.loadDraft(draftId)
    }
} 