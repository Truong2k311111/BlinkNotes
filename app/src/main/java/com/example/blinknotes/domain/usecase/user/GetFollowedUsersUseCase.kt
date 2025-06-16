package com.example.blinknotes.domain.usecase.user

import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.UserRepository
import javax.inject.Inject

class GetFollowedUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): List<User> {
        return userRepository.getFollowedUsers()
    }
} 