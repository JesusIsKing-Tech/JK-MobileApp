package com.example.jkconect.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.LoginApiService
import com.example.jkconect.data.api.LoginRequest
import com.example.jkconect.data.api.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Response

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val token: String? = null,
    val userId: Int? = null
)

class LoginViewModel(private val loginApiService: LoginApiService) : ViewModel(), KoinComponent {
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    fun onEmailChanged(newEmail: String) {
        _loginUiState.update { it.copy(email = newEmail, error = null) }
    }

    fun onSenhaChanged(newSenha: String) {
        _loginUiState.update { it.copy(senha = newSenha, error = null) }
    }

    fun resetState() {
        _loginUiState.value = LoginUiState()
    }

    fun login(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = LoginRequest(
                    email = _loginUiState.value.email,
                    senha = _loginUiState.value.senha
                )
                val response = loginApiService.login(request)
                val tokenJWT = response.body()?.token
                if (response.isSuccessful) {
                    Log.d("LoginScreen", "TOKEN GERADO: $tokenJWT")
                    val loginResponse = response.body()
                    loginResponse?.let {
                        _loginUiState.value = _loginUiState.value.copy( // Use _loginUiState.value.copy
                            isLoading = false,
                            token = tokenJWT,
                            userId = it.userId
                        )
                        val userId = it.userId
                        if (!it.token.isNullOrBlank() && userId != null) {
                            onSuccess(userId)
                        } else {
                            _loginUiState.value = _loginUiState.value.copy( // Use _loginUiState.value.copy
                                isLoading = false,
                                error = "Token ou ID de usuário não recebidos na resposta."
                            )
                            onError("Token ou ID de usuário não recebidos na resposta.")
                        }
                    } ?: run {
                        _loginUiState.value = _loginUiState.value.copy( // Use _loginUiState.value.copy
                            isLoading = false,
                            error = "Resposta de login vazia."
                        )
                        onError("Resposta de login vazia.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Erro de login: ${response.code()} - $errorBody"
                    _loginUiState.value = _loginUiState.value.copy( // Use _loginUiState.value.copy
                        isLoading = false,
                        error = errorMessage
                    )
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy( // Use _loginUiState.value.copy
                    isLoading = false,
                    error = "Erro de rede ou interno: ${e.localizedMessage}"
                )
                onError("Erro de rede ou interno: ${e.localizedMessage}")
            }
        }
    }

}