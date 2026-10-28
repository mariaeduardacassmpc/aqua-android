package com.maria.aqua.dto.auth

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)