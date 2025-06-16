package com.example.blinknotes.domain.usecase.post

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.PostRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(pageSize: Int, startAfterPostId: String?): Pair<List<Post>, String?> {
        return postRepository.getFeedPostsPaginated(pageSize, startAfterPostId)
    }
} 