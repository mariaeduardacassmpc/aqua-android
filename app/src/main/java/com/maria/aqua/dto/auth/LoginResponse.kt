package com.maria.aqua.dto.auth

data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: UserResponse
)