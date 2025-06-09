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
import okhttp3.ResponseBody
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
    }

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

    suspend fun imagemEvento(id: Int): ResponseBody {
        return api.getFotoEvento(id)
    }

    fun isEventoFavorito(eventoId: Int): Boolean {
        return _eventosFavoritos.value.contains(eventoId)
    }

}