package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class SearchPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(query: String): List<Post> {
        return postRepository.searchPosts(query)
    }
} 