package com.example.blinknotes.domain.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userIdCmt: String = "",
    val imageUrls: List<String> = emptyList(),
    val firstImageUrl: String = "",
    val caption: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val visibility: String = "public",
    val tags: List<String> = emptyList(),
    val status: String = "active",
    val isReported: Boolean = false,
    val isHidden: Boolean = false,
    val isBlocked: Boolean = false,
    val isPinned: Boolean = false,
    val isSaved: Boolean = false,
    val isLiked: Boolean = false,
    val isCommented: Boolean = false,
    val isShared: Boolean = false,

)