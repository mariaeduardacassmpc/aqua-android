package com.maria.aqua.dto.auth

data class PasswordResetRequest(
    val code: String,
    val newPassword: String
)