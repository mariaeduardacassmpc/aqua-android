package com.maria.aqua.data.remote.model.auth

data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: UserResponse
)