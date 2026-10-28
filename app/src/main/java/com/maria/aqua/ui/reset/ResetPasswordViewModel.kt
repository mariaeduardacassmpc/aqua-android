package com.maria.aqua.ui.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maria.aqua.data.repository.ResetPasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val repository: ResetPasswordRepository =
        ResetPasswordRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun resetPassword(
        email: String,
        code: String,
        password: String,
        confirmPassword: String
    ) {
        val error = when {
            email.isBlank() ->
                "Informe seu e-mail na etapa anterior."

            code.isBlank() || password.isBlank() ||
                    confirmPassword.isBlank() ->
                "Preencha todos os campos."

            password.length < 8 ->
                "A senha deve ter pelo menos 8 caracteres."

            password != confirmPassword ->
                "As senhas não coincidem."

            else -> null
        }

        if (error != null) {
            _uiState.value = ResetPasswordUiState(error = error)
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState(isLoading = true)

            try {
                val response = repository.resetPassword(code, password)

                _uiState.value = if (response.isSuccessful) {
                    ResetPasswordUiState(success = true)
                } else {
                    ResetPasswordUiState(
                        error = when (response.code()) {
                            400 -> "Código inválido ou expirado."
                            404 -> "Conta ou código não encontrado."
                            else -> "Não foi possível redefinir a senha. Tente novamente."
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ResetPasswordUiState(
                    error = "Erro de conexão com o servidor. Tente novamente."
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = ResetPasswordUiState()
    }
}
