import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Classe para integração com o backend Mistral AI
 */
class MistralAIIntegration(private val backendUrl: String = "http://10.18.32.40:80") {
    // Cliente HTTP para chamadas de API
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Histórico de mensagens para contexto
    private val mensagensHistorico = mutableListOf<Pair<String, String>>() // (role, content)

    /**
     * Envia uma mensagem para a API Mistral e retorna a resposta
     */
    suspend fun enviarMensagem(texto: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Adiciona a mensagem do usuário ao histórico
                mensagensHistorico.add(Pair("user", texto))

                // Prepara o corpo da requisição
                val jsonBody = JSONObject().apply {
                    put("messages", JSONArray().apply {
                        mensagensHistorico.forEach { (role, content) ->
                            put(JSONObject().apply {
                                put("role", role)
                                put("content", content)
                            })
                        }
                    })
                }

                // Cria a requisição
                val request = Request.Builder()
                    .url("$backendUrl/api/chat")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                // Executa a requisição
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Erro na requisição: ${response.code}")
                    }

                    val responseBody = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseBody)

                    // Extrai a resposta do JSON
                    val resposta = if (jsonResponse.has("choices") && jsonResponse.getJSONArray("choices").length() > 0) {
                        val choice = jsonResponse.getJSONArray("choices").getJSONObject(0)
                        if (choice.has("message") && choice.getJSONObject("message").has("content")) {
                            choice.getJSONObject("message").getString("content")
                        } else {
                            "Desculpe, não consegui processar sua mensagem."
                        }
                    } else if (jsonResponse.has("response")) {
                        // Formato alternativo que pode ser usado pelo backend
                        jsonResponse.getString("response")
                    } else {
                        "Desculpe, não consegui processar sua mensagem."
                    }

                    // Adiciona a resposta ao histórico
                    mensagensHistorico.add(Pair("assistant", resposta))

                    resposta
                }
            } catch (e: Exception) {
                "Erro ao se comunicar com o assistente: ${e.message}"
            }
        }
    }

    /**
     * Limpa o histórico de mensagens
     */
    fun limparHistorico() {
        mensagensHistorico.clear()
    }
}