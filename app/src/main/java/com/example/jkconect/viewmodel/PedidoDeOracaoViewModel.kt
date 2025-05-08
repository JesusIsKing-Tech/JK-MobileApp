import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.PedidoOracaoApiService
import kotlinx.coroutines.launch

class PedidoDeOracaoViewModel(
    private val apiService: PedidoOracaoApiService
) : ViewModel() {

    fun enviarPedidoOracao(
        pedido: PedidoOracao,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (pedido.idUsuario == null || pedido.idUsuario == -1) {
            val errorMessage = "O campo userId é obrigatório e não pode ser nulo."
            Log.e("PedidoDeOracaoViewModel", errorMessage)
            onError(errorMessage)
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.cadastrarPedidoOracao(pedido)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                    Log.e("PedidoDeOracaoViewModel", "Erro ao enviar pedido: $errorMessage")
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("PedidoDeOracaoViewModel", "Erro ao enviar pedido: ${e.message}", e)
                onError(e.message ?: "Erro desconhecido")
            }
        }
    }
}