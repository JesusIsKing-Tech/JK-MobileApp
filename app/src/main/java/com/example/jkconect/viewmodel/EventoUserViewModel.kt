package com.example.jkconect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.EventoApiService
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.model.EventoUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "EventoUserViewModel"

class EventoUserViewModel(
    private val api: EventoApiService,
    private val userViewModel: UserViewModel,
    private val eventoViewModel: EventoViewModel
) : ViewModel() {

    // Lista de IDs de eventos curtidos pelo usuário
    private val _eventosCurtidos = MutableStateFlow<List<Int>>(emptyList())
    val eventosCurtidos: StateFlow<List<Int>> = _eventosCurtidos.asStateFlow()

    // Lista de IDs de eventos com presença confirmada
    private val _eventosConfirmados = MutableStateFlow<List<Int>>(emptyList())
    val eventosConfirmados: StateFlow<List<Int>> = _eventosConfirmados.asStateFlow()

    // Estado de carregamento
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensagem de erro
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mensagem de sucesso
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Carregar eventos curtidos e confirmados quando o ViewModel for inicializado
        carregarEventosCurtidos()
        carregarEventosConfirmados()
    }

    /**
     * Carrega a lista de eventos curtidos pelo usuário
     */
    fun carregarEventosCurtidos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Aqui você precisaria implementar uma chamada para obter os eventos curtidos
                // Como não temos um endpoint específico, vamos simular com uma lista vazia
                _eventosCurtidos.value = emptyList()
                Log.d(TAG, "Lista de eventos curtidos carregada")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar eventos curtidos", e)
                _errorMessage.value = "Erro ao carregar eventos curtidos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carrega a lista de eventos com presença confirmada pelo usuário
     */
    fun carregarEventosConfirmados() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userViewModel.userId.value
                if (userId > 0) {
                    Log.d(TAG, "Carregando eventos confirmados para usuário ID: $userId")

                    // Chamar o endpoint para obter eventos confirmados
                    val eventosConfirmados = api.getEventosConfirmados(userId)
                    _eventosConfirmados.value = eventosConfirmados

                    Log.d(TAG, "Eventos confirmados carregados: ${eventosConfirmados.size} eventos")
                    Log.d(TAG, "IDs dos eventos confirmados: $eventosConfirmados")
                } else {
                    Log.e(TAG, "ID do usuário inválido: $userId")
                    _errorMessage.value = "ID de usuário inválido"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar eventos confirmados", e)
                _errorMessage.value = "Erro ao carregar eventos confirmados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
     * Verifica se um evento está curtido
     */
    fun isEventoCurtido(eventoId: Int): Boolean {
        return _eventosCurtidos.value.contains(eventoId)
    }

    /**
     * Verifica se um evento tem presença confirmada
     */
    fun isEventoConfirmado(eventoId: Int): Boolean {
        return _eventosConfirmados.value.contains(eventoId)
    }

    /**
     * Alterna o status de curtida de um evento
     */
    fun alternarFavorito(usuarioId: Int, eventoId: Int) {
        viewModelScope.launch {
            if (usuarioId <= 0 || eventoId <= 0) {
                Log.e(TAG, "ID de usuário ou evento inválido: usuarioId=$usuarioId, eventoId=$eventoId")
                _errorMessage.value = "ID de usuário ou evento inválido"
                return@launch
            }

            _isLoading.value = true
            try {
                val isCurtido = _eventosCurtidos.value.contains(eventoId)

                if (isCurtido) {
                    // Remover curtida
                    Log.d(TAG, "Removendo curtida do evento $eventoId pelo usuário $usuarioId")
                    api.removerCurtida(usuarioId, eventoId)

                    // Atualizar lista de eventos curtidos
                    val novaLista = _eventosCurtidos.value.toMutableList()
                    novaLista.remove(eventoId)
                    _eventosCurtidos.value = novaLista

                    Log.d(TAG, "Curtida removida com sucesso")
                    _successMessage.value = "Evento removido dos favoritos"
                } else {
                    // Adicionar curtida
                    Log.d(TAG, "Adicionando curtida ao evento $eventoId pelo usuário $usuarioId")
                    api.registrarCurtida(usuarioId, eventoId)

                    // Atualizar lista de eventos curtidos
                    val novaLista = _eventosCurtidos.value.toMutableList()
                    novaLista.add(eventoId)
                    _eventosCurtidos.value = novaLista

                    Log.d(TAG, "Curtida adicionada com sucesso")
                    _successMessage.value = "Evento adicionado aos favoritos"
                }

                // Recarregar eventos para atualizar a UI
                eventoViewModel.carregarEventos()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao alternar curtida: ${e.message}", e)
                _errorMessage.value = "Erro ao alternar curtida: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Confirma presença em um evento
     */
    fun confirmarPresenca(usuarioId: Int, eventoId: Int) {
        viewModelScope.launch {
            if (usuarioId <= 0 || eventoId <= 0) {
                Log.e(TAG, "ID de usuário ou evento inválido: usuarioId=$usuarioId, eventoId=$eventoId")
                _errorMessage.value = "ID de usuário ou evento inválido"
                return@launch
            }

            _isLoading.value = true
            try {
                Log.d(TAG, "Confirmando presença no evento $eventoId pelo usuário $usuarioId")
                api.registrarPresenca(usuarioId, eventoId)

                // Atualizar lista de eventos confirmados
                val novaLista = _eventosConfirmados.value.toMutableList()
                novaLista.add(eventoId)
                _eventosConfirmados.value = novaLista

                Log.d(TAG, "Presença confirmada com sucesso")
                _successMessage.value = "Presença confirmada com sucesso!"

                // Recarregar contagem de presenças
                eventoViewModel.contarConfirmacoesPresenca(eventoId)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao confirmar presença: ${e.message}", e)
                _errorMessage.value = "Erro ao confirmar presença: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cancela presença em um evento
     */
    fun cancelarPresenca(usuarioId: Int, eventoId: Int) {
        viewModelScope.launch {
            if (usuarioId <= 0 || eventoId <= 0) {
                Log.e(TAG, "ID de usuário ou evento inválido: usuarioId=$usuarioId, eventoId=$eventoId")
                _errorMessage.value = "ID de usuário ou evento inválido"
                return@launch
            }

            _isLoading.value = true
            try {
                Log.d(TAG, "Cancelando presença no evento $eventoId pelo usuário $usuarioId")
                api.cancelarPresenca(usuarioId, eventoId)

                // Atualizar lista de eventos confirmados
                val novaLista = _eventosConfirmados.value.toMutableList()
                novaLista.remove(eventoId)
                _eventosConfirmados.value = novaLista

                Log.d(TAG, "Presença cancelada com sucesso")
                _successMessage.value = "Presença cancelada com sucesso"

                // Recarregar contagem de presenças
                eventoViewModel.contarConfirmacoesPresenca(eventoId)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao cancelar presença: ${e.message}", e)
                _errorMessage.value = "Erro ao cancelar presença: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpa a mensagem de erro
     */
    fun limparErro() {
        _errorMessage.value = null
    }

    /**
     * Limpa a mensagem de sucesso
     */
    fun limparSucesso() {
        _successMessage.value = null
    }
}