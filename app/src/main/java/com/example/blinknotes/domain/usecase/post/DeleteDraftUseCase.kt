package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class DeleteDraftUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(draftId: String) {
        postRepository.deleteDraft(draftId)
    }
} 