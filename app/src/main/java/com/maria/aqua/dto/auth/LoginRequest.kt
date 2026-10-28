package com.maria.aqua.dto.auth

data class LoginRequest(
    val email: String,
    val password: String
)