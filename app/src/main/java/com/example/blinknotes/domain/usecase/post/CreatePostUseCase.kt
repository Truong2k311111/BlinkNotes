package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(caption: String, content: String, visibility: String, imageUris: List<String>): Post {
        return postRepository.createPost(caption, content, visibility, imageUris)
    }
} 