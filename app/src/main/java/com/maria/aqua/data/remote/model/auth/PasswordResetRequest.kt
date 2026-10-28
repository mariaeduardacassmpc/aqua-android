package com.maria.aqua.data.remote.model.auth

data class PasswordResetRequest(
    val code: String,
    val newPassword: String
)