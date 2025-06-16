package com.example.blinknotes.domain.model

sealed class AuthError {
    data class AuthenticationError(val message: String) : AuthError()
    data class NetworkError(val message: String) : AuthError()
    data class ValidationError(val message: String) : AuthError()
    data class UnknownError(val message: String) : AuthError()
} 