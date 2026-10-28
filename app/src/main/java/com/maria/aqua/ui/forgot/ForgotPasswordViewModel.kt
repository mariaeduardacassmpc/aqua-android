package com.maria.aqua.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maria.aqua.data.repository.ForgotPasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: ForgotPasswordRepository =
        ForgotPasswordRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun sendRecoveryEmail(email: String) {
        if (email.isBlank()) {
            _uiState.value = ForgotPasswordUiState(
                error = "Informe seu e-mail."
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ForgotPasswordUiState(
                error = "Informe um e-mail válido."
            )
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState(isLoading = true)

            try {
                val response = repository.forgotPassword(email)

                _uiState.value = if (response.isSuccessful) {
                    ForgotPasswordUiState(success = true)
                } else {
                    ForgotPasswordUiState(
                        error = "Não foi possível solicitar a recuperação. Tente novamente."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState(
                    error = "Não foi possível conectar à API."
                )
            }
        }
    }
}