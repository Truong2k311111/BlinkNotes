package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class GetPostsByUserIdsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(userIds: List<String>): List<Post> {
        return postRepository.getPostsByUserIds(userIds)
    }
} 