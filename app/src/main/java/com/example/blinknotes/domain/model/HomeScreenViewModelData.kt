package com.example.blinknotes.domain.model

data class HomeScreenViewModelData(
    val listFeed : List<Feed> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val imageLink: String? = null,
    val avatarLink: String? = null,
    val status: String? = null,
    val numBerHeart: Int? = 0,
    val userName: String? = null,
)