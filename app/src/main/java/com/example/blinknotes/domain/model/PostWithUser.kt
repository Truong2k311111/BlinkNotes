package com.example.blinknotes.domain.model

data class PostWithUser(
    val post: Post,
    val user: User?
)