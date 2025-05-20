package com.example.jkconect.viewmodel

import Evento
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.EventoApiService
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.model.EventoUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val TAG = "EventoUserViewModel"

class EventoUserViewModel(
    private val api: EventoApiService,
    private val userViewModel: UserViewModel,
    private val eventoViewModel: EventoViewModel

) : ViewModel() {

    val eventosCurtidos = mutableStateListOf<Evento>()

    var eventosCurtidoId = mutableStateListOf<Int>()


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


    fun carregarEventosCurtidos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Carregando eventos curtidos do backend")
                val lista = api.getEventosCurtidos(userViewModel.userId.value)
                Log.d(TAG, "Total de Eventos recebidos: ${lista.size}")

                // Adicione este log para verificar os dados de cada evento
                lista.forEach { evento ->
                    Log.d(TAG, "Evento recebido: ID=${evento.id}, Título=${evento.titulo}, Descrição=${evento.descricao}")
                }

                eventosCurtidos.clear()
                eventosCurtidos.addAll(lista)
            } catch (e: HttpException) {
                Log.e(TAG, "Erro HTTP ao carregar eventos: ${e.code()}", e)
                _errorMessage.value = "Erro ao carregar eventos: ${e.message()}"
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar eventos", e)
                _errorMessage.value = "Erro ao carregar eventos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
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



    fun carregarIdEventosCurtidos(){
        carregarEventosCurtidos()
        eventosCurtidoId = eventosCurtidos.map { it.id } as SnapshotStateList<Int>
        }

    fun isEventoCurtido(eventoId: Int): Boolean {
        return eventosCurtidos.any { it.id == eventoId }
    }

    fun isEventoConfirmado(eventoId: Int): Boolean {
        return _eventosConfirmados.value.contains(eventoId)
    }


    /**
     * Alterna o status de curtida de um evento
     */
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