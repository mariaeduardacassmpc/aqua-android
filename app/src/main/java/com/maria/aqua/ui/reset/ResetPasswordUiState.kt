package com.maria.aqua.ui.reset

data class ResetPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
