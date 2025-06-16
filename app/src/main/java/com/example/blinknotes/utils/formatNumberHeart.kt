package com.example.blinknotes.utils

fun formatNumberHeart(number: Int?): String {
    if (number != null) {
        return when {
            number >= 10_000 -> String.format("%.1f N", number / 10000f)
            number >= 1000 -> String.format("%,.3f", number / 1000f)
            else -> number.toString()
        }
    }
    return "0"
}