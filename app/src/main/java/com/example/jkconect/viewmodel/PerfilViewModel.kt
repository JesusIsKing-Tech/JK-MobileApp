package com.example.jkconect.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.ApiService
import com.example.jkconect.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.jkconect.data.api.RetrofitClient
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

data class PerfilUiState(
    val userId: Int? = null,
    val usuario: Usuario? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PerfilViewModel(
private val apiService: ApiService,
private val sharedPreferences: SharedPreferences
) : ViewModel(), KoinComponent {

    private val _perfilUiState = MutableStateFlow(PerfilUiState())
    val perfilUiState: StateFlow<PerfilUiState> = _perfilUiState.asStateFlow()

    fun buscarPerfil(userId: Int) {
        viewModelScope.launch {
            _perfilUiState.update { it.copy(isLoading = true, error = null, userId = userId) }
            try {
                val authToken = sharedPreferences.getString("jwt_token", null)
                if (authToken.isNullOrBlank()) {
                    _perfilUiState.update { it.copy(isLoading = false, error = "Token de autenticação não encontrado.") }
                    return@launch
                }
                val response = apiService.getPerfil(userId, "Bearer $authToken")
                if (response.isSuccessful) {
                    _perfilUiState.update { it.copy(isLoading = false, usuario = response.body()) }
                } else {
                    _perfilUiState.update { it.copy(isLoading = false, error = "Erro ao buscar perfil: Código ${response.code()}") }
                }
            } catch (e: Exception) {
                _perfilUiState.update { it.copy(isLoading = false, error = "Erro ao buscar perfil: ${e.message}") }
            }
        }
    }

    fun resetState() {
        _perfilUiState.value = PerfilUiState()
    }
}