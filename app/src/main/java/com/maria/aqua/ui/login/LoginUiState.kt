package com.maria.aqua.ui

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val token: String? = null
)
