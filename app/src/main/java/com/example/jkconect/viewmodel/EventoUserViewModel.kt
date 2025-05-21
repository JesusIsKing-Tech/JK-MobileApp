package com.example.jkconect.viewmodel

import Evento
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.EventoApiService
import com.example.jkconect.data.api.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val TAG = "EventoUserViewModel"

class EventoUserViewModel(
    private val api: EventoApiService,
    private val userViewModel: UserViewModel,
    private val eventoViewModel: EventoViewModel

) : ViewModel() {

    var eventosCurtidos = mutableStateListOf<Evento>()

    // Lista de IDs de eventos favoritos como StateFlow para reatividade
    private val _eventosCurtidosIds = MutableStateFlow<List<Int>>(emptyList())
    val eventosCurtidosIds: StateFlow<List<Int>> = _eventosCurtidosIds.asStateFlow()

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
                    Log.d(
                        TAG,
                        "Evento recebido: ID=${evento.id}, Título=${evento.titulo}, Descrição=${evento.descricao}"
                    )
                }

                eventosCurtidos.clear()
                eventosCurtidos.addAll(lista)

                // Extrair IDs e atualizar o StateFlow
                val ids = lista.mapNotNull { it.id }
                _eventosCurtidosIds.value = ids

                Log.d(TAG, "IDs de eventos curtidos atualizados: ${_eventosCurtidosIds.value}")

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

    // Adicione este StateFlow para armazenar os eventos completos
    var _eventosConfirmadosCompletos = mutableStateListOf<Evento>()

    // Adicionar StateFlow para expor os eventos confirmados completos
    var _eventosConfirmadosCompletosFlow = MutableStateFlow<List<Evento>>(emptyList())
    var eventosConfirmadosCompletos: StateFlow<List<Evento>> =
        _eventosConfirmadosCompletosFlow.asStateFlow()

    fun carregarEventosConfirmados() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userViewModel.userId.value
                if (userId > 0) {
                    Log.d(TAG, "Carregando eventos confirmados para usuário ID: $userId")

                    // Chamar o endpoint para obter eventos confirmados
                    val eventosConfirmadosLista = api.getEventosConfirmados(userId)

                    // Armazenar a lista completa de eventos
                    _eventosConfirmadosCompletos.clear()
                    _eventosConfirmadosCompletos.addAll(eventosConfirmadosLista)

                    // Atualizar o StateFlow com a lista completa
                    _eventosConfirmadosCompletosFlow.value = eventosConfirmadosLista

                    // Extrair apenas os IDs para manter compatibilidade com o código existente
                    val eventosIds = eventosConfirmadosLista.mapNotNull { it.id }
                    _eventosConfirmados.value = eventosIds

                    Log.d(
                        TAG,
                        "Eventos confirmados carregados: ${eventosConfirmadosLista.size} eventos"
                    )
                    Log.d(TAG, "IDs dos eventos confirmados: $eventosIds")
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

    fun isEventoConfirmado(eventoId: Int): Boolean {
        return _eventosConfirmados.value.contains(eventoId)
    }

    fun confirmarPresenca(usuarioId: Int, eventoId: Int) {
        viewModelScope.launch {
            if (usuarioId <= 0 || eventoId <= 0) {
                Log.e(
                    TAG,
                    "ID de usuário ou evento inválido: usuarioId=$usuarioId, eventoId=$eventoId"
                )
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

                // Adicionar o evento à lista completa se não estiver presente
                val evento = eventoViewModel.eventos.find { it.id == eventoId }
                if (evento != null && !_eventosConfirmadosCompletos.any { it.id == eventoId }) {
                    _eventosConfirmadosCompletos.add(evento)
                    _eventosConfirmadosCompletosFlow.value = _eventosConfirmadosCompletos.toList()
                }

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

                // Atualizar lista de IDs
                val novaLista = _eventosConfirmados.value.toMutableList()
                novaLista.remove(eventoId)
                _eventosConfirmados.value = novaLista

                // Remover o evento da lista completa
                _eventosConfirmadosCompletos.removeIf { it.id == eventoId }
                _eventosConfirmadosCompletosFlow.value = _eventosConfirmadosCompletos.toList()


                Log.d(TAG, "Presença cancelada com sucesso")
                _successMessage.value = "Presença cancelada com sucesso"
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao cancelar presença: ${e.message}", e)
                _errorMessage.value = "Erro ao cancelar presença: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun limparErro() {
        _errorMessage.value = null
    }

    fun limparSucesso() {
        _successMessage.value = null
    }

    fun alternarCurtir(userId: Int, eventoId: Int) {
        viewModelScope.launch {
            try {
                val estaCurtido = _eventosCurtidosIds.value.contains(eventoId)

                if (estaCurtido) {
                    Log.d(TAG, "Removendo evento $eventoId dos favoritos do usuário $userId")
                    api.removerCurtida(userId, eventoId)

                    // Atualizar a lista de IDs
                    val novaLista = _eventosCurtidosIds.value.toMutableList()
                    novaLista.remove(eventoId)
                    _eventosCurtidosIds.value = novaLista

                    // Atualizar a lista de eventos
                    eventosCurtidos.removeIf { it.id == eventoId }

                    Log.d(
                        TAG,
                        "Evento removido dos favoritos. IDs atualizados: ${_eventosCurtidosIds.value}"
                    )
                } else {
                    Log.d(TAG, "Adicionando evento $eventoId aos favoritos do usuário $userId")
                    api.curtirEvento(userId, eventoId)

                    // Atualizar a lista de IDs
                    val novaLista = _eventosCurtidosIds.value.toMutableList()
                    novaLista.add(eventoId)
                    _eventosCurtidosIds.value = novaLista

                    // Atualizar a lista de eventos
                    val evento = eventoViewModel.eventos.find { it.id == eventoId }
                    if (evento != null) {
                        eventosCurtidos.add(evento)
                    }

                    Log.d(
                        TAG,
                        "Evento adicionado aos favoritos. IDs atualizados: ${_eventosCurtidosIds.value}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao alternar favorito: ${e.message}", e)
                _errorMessage.value = "Erro ao alternar favorito: ${e.message}"
            }
        }
    }

    // Método para verificar se um evento está nos favoritos, retornando um StateFlow
    fun isEventoFavoritoFlow(eventoId: Int?): StateFlow<Boolean> {
        if (eventoId == null) return MutableStateFlow(false)

        // Criar um StateFlow derivado que mapeia a lista de IDs para um booleano
        return _eventosCurtidosIds.map { ids ->
            ids.contains(eventoId)
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    }

    // Adicionar estes métodos ao EventoUserViewModel para permitir atualização direta das listas

    // Método para atualizar a lista de IDs de eventos confirmados
    fun atualizarEventosConfirmados(novaLista: List<Int>) {
        _eventosConfirmados.value = novaLista
    }

    // Método para atualizar a lista completa de eventos confirmados
    fun atualizarEventosConfirmadosCompletos(novaLista: List<Evento>) {
        _eventosConfirmadosCompletos.clear()
        _eventosConfirmadosCompletos.addAll(novaLista)
    }
}