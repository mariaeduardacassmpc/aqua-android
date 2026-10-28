package com.maria.aqua.ui.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maria.aqua.data.repository.RegisterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: RegisterRepository = RegisterRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val error = when {
            name.isBlank() || email.isBlank() ||
                    password.isBlank() || confirmPassword.isBlank() ->
                "Preencha todos os campos."

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Informe um e-mail válido."

            password != confirmPassword ->
                "As senhas não coincidem."

            else -> null
        }

        if (error != null) {
            _uiState.value = RegisterUiState(error = error)
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)

            try {
                val response = repository.register(name, email, password)

                _uiState.value = if (response.isSuccessful) {
                    RegisterUiState(success = true)
                } else {
                    RegisterUiState(
                        error = when (response.code()) {
                            400, 409 -> "Não foi possível cadastrar. Verifique os dados ou se o e-mail já está cadastrado."
                            else -> "Não foi possível cadastrar. Erro ${response.code()}."
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(
                    error = "Falha de conexão com a API."
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = RegisterUiState()
    }
}
