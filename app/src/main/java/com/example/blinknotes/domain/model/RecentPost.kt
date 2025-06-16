package com.example.blinknotes.domain.model

import java.util.Date

data class RecentPost(
    val id: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: Date
)