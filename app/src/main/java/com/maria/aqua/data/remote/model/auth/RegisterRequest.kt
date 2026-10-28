package com.maria.aqua.data.remote.model.auth

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)