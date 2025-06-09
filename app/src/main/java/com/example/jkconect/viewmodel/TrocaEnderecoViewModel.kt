package com.example.jkconect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.TrocaDeEnderecoApiService
import com.example.jkconect.model.Endereco
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado UI para controlar o estado da operação
data class TrocaEnderecoUiState(
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val chamadoId: Int? = null
)

class TrocaEnderecoViewModel(
    private val apiService: TrocaDeEnderecoApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrocaEnderecoUiState())
    val uiState: StateFlow<TrocaEnderecoUiState> = _uiState.asStateFlow()

    fun atualizarEndereco(userId: Int, endereco: Endereco) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, erro = null)

            try {
                Log.d("TrocaEnderecoViewModel", "Enviando atualização de endereço para usuário $userId")
                val response = apiService.abrirChamado(userId, endereco)

                if (response.isSuccessful) {
                    val enderecoAtualizado = response.body()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sucesso = true,
                        erro = null
                    )
                    Log.d("TrocaEnderecoViewModel", "Endereço atualizado com sucesso")
                } else {
                    val erro = "Erro ao atualizar endereço: ${response.code()} - ${response.message()}"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        erro = erro
                    )
                    Log.e("TrocaEnderecoViewModel", erro)
                }
            } catch (e: Exception) {
                val erro = "Erro de conexão: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    erro = erro
                )
                Log.e("TrocaEnderecoViewModel", "Erro ao atualizar endereço", e)
            }
        }
    }
}