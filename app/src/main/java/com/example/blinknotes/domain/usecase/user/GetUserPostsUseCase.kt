package com.example.blinknotes.domain.usecase.user

import com.example.blinknotes.domain.model.Post
import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class GetUserPostsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): List<Post> {
        return userRepository.getUserPosts(userId)
    }
} 