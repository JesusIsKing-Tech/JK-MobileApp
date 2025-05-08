package com.example.jkconect.viewmodel
import Evento
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.EventoApiService
import com.example.jkconect.data.api.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Calendar
import java.util.Date

private const val TAG = "EventoViewModel"

class EventoViewModel(
    private val api: EventoApiService,
    private val userViewModel: UserViewModel
) : ViewModel()  {
    // O resto do código permanece o mesmo, mas agora você não precisa usar o inject do Koin

    // Lista de eventos
    val eventos = mutableStateListOf<Evento>()

    // Estado de carregamento
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Contagem de presenças para o evento atual
    private val _contagemPresencas = MutableStateFlow<Long>(0)
    val contagemPresencas: StateFlow<Long> = _contagemPresencas.asStateFlow()

    // Lista de IDs de eventos favoritos
    private val _eventosFavoritos = MutableStateFlow<List<Int>>(emptyList())
    val eventosFavoritos: StateFlow<List<Int>> = _eventosFavoritos.asStateFlow()

    // Mensagem de erro
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Filtro atual
    private val _filtroAtual = MutableStateFlow("Todos")
    val filtroAtual: StateFlow<String> = _filtroAtual.asStateFlow()

    init {
        Log.d(TAG, "Inicializando EventoViewModel")
        carregarEventos()
        carregarFavoritos()
    }

    /**
     * Carrega todos os eventos do backend
     */
    fun carregarEventos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Carregando eventos do backend")
                val lista = api.getEventos()
                Log.d(TAG, "Total de Eventos recebidos: ${lista.size}")

                eventos.clear()
                eventos.addAll(lista)
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

    /**
     * Carrega eventos da semana atual
     */
/**
    fun carregarEventosSemana() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Carregando eventos da semana")
                val lista = api.getEventosSemana()
                Log.d(TAG, "Eventos da semana recebidos: ${lista.size}")

                eventos.clear()
                eventos.addAll(lista)
                _filtroAtual.value = "Esta semana"
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar eventos da semana", e)
                _errorMessage.value = "Erro ao carregar eventos da semana: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
 */
    /**
     * Carrega a lista de eventos favoritos do usuário
     */
    // Adicione esta função ao EventoViewModel
    fun carregarFavoritos() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Carregando lista de favoritos")
                // Aqui você precisaria implementar uma chamada para obter os favoritos do usuário
                // Como não temos um endpoint específico, vamos simular com uma lista vazia
                // _eventosFavoritos.value = emptyList()

                // Para teste, vamos adicionar alguns IDs de eventos como favoritos
                val listaEventos = eventos.map { it.id }.filterNotNull()
                if (listaEventos.isNotEmpty()) {
                    _eventosFavoritos.value = listaEventos.take(2) // Pega os primeiros 2 eventos como favoritos
                    Log.d(TAG, "Favoritos carregados: ${_eventosFavoritos.value}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar favoritos", e)
            }
        }
    }

    /**
     * Conta o número de confirmações de presença para um evento
     */
    suspend fun contarConfirmacoesPresenca(eventoId: Int): Long {
        return try {
//            Log.d(TAG, "Contando confirmações para evento $eventoId")
            val contagem = api.contarConfirmacoesPresenca(eventoId)
            Log.d(TAG, "Contagem recebida: $contagem para evento $eventoId")
            _contagemPresencas.value = contagem
            contagem
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar confirmações para evento $eventoId", e)
            _contagemPresencas.value = 0
            0L
        }
    }

    /**
     * Alterna o status de favorito (curtida) de um evento
     */
    /**
     * Registra presença do usuário em um evento
     */

//    fun filtrarEventosPorPeriodo(periodo: String) {
//        viewModelScope.launch {
//            _isLoading.value = true
//
//            try {
//                Log.d(TAG, "Filtrando eventos por período: $periodo")
//
//                when (periodo) {
//                    "Esta semana" -> {
//                        carregarEventosSemana()
//                    }
//                    "Este Mês" -> {
//                        // Carregar todos e filtrar localmente
//                        val lista = api.getEventos()
//
//                        val hoje = Calendar.getInstance()
//                        val fimDoMes = Calendar.getInstance()
//                        fimDoMes.add(Calendar.MONTH, 1)
//
//                        val eventosFiltrados = lista.filter { evento ->
//                            evento.data?.let { data ->
//                                data.after(hoje.time) && data.before(fimDoMes.time)
//                            } ?: false
//                        }
//
//                        Log.d(TAG, "Eventos do mês filtrados: ${eventosFiltrados.size}")
//
//                        eventos.clear()
//                        eventos.addAll(eventosFiltrados)
//                        _filtroAtual.value = periodo
//                    }
//                    else -> {
//                        carregarEventos()
//                        _filtroAtual.value = "Todos"
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Erro ao filtrar eventos por período", e)
//                _errorMessage.value = "Erro ao filtrar eventos: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

    /**
     * Busca eventos por texto
     */
    fun buscarEventos(texto: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Buscando eventos com texto: '$texto'")

                // Carregar todos os eventos e filtrar localmente
                val lista = api.getEventos()

                val eventosFiltrados = if (texto.isEmpty()) {
                    lista
                } else {
                    lista.filter { evento ->
                        evento.titulo?.contains(texto, ignoreCase = true) == true ||
                                evento.endereco.contains(texto, ignoreCase = true) ||
                                evento.descricao?.contains(texto, ignoreCase = true) == true
                    }
                }

                Log.d(TAG, "Eventos filtrados por texto: ${eventosFiltrados.size}")

                eventos.clear()
                eventos.addAll(eventosFiltrados)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar eventos", e)
                _errorMessage.value = "Erro ao buscar eventos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtém um evento pelo ID
     */
    fun getEventoPorId(eventoId: Int): Evento? {
        return eventos.find { it.id == eventoId }
    }

    /**
     * Verifica se um evento é favorito
     */
    fun isEventoFavorito(eventoId: Int): Boolean {
        return _eventosFavoritos.value.contains(eventoId)
    }

    /**
     * Limpa a mensagem de erro
     */
    fun limparErro() {
        _errorMessage.value = null
    }

}