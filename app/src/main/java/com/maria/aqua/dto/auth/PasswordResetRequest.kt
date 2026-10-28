package com.maria.aqua.dto.auth

data class PasswordResetRequest(
    val token: String,
    val newPassword: String
)