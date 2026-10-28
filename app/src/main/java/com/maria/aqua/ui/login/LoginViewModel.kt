package com.maria.aqua.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maria.aqua.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                error = "Preencha o e-mail e a senha."
            )
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            try {
                val response = repository.login(email, password)

                if (response.isSuccessful) {
                    val token = response.body()?.token

                    if (token.isNullOrBlank()) {
                        _uiState.value = LoginUiState(
                            error = "A API não retornou um token válido."
                        )
                    } else {
                        _uiState.value = LoginUiState(token = token)
                    }
                } else {
                    _uiState.value = LoginUiState(
                        error = when (response.code()) {
                            400, 401 -> "E-mail ou senha inválidos."
                            else -> "Não foi possível entrar. Erro ${response.code()}."
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    error = "Não foi possível conectar à API. Verifique sua conexão."
                )
            }
        }
    }
}
