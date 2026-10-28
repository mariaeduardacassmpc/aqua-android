package com.maria.aqua.data.remote.model.auth

data class LoginRequest(
    val email: String,
    val password: String
)