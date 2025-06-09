package com.example.jkconect.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.jkconect.data.api.PerfilApiService
import com.example.jkconect.model.UsuarioResponseDto
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream
import okhttp3.ResponseBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PerfilUiState(
    val userId: Int? = null,
    val usuario: Usuario? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val familia: List<UsuarioResponseDto>? = null,
    val isFamiliaLoading: Boolean = false,
    val familiaError: String? = null,
    val isUpdatingPreferences: Boolean = false,
    val updatePreferencesError: String? = null,
    val updatePreferencesSuccess: Boolean = false
)

class PerfilViewModel(
    private val perfilApiService: PerfilApiService,
    private val sharedPreferences: SharedPreferences,
    private val applicationContext: Context
) : ViewModel() {

    private val _perfilUiState = MutableStateFlow(PerfilUiState())
    val perfilUiState: StateFlow<PerfilUiState> = _perfilUiState.asStateFlow()

    private val _profileImageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val profileImageBitmap: StateFlow<ImageBitmap?> = _profileImageBitmap.asStateFlow()

    fun buscarPerfil(userId: Int) {
        viewModelScope.launch {
            _perfilUiState.update { it.copy(isLoading = true, error = null, userId = userId) }
            try {
                val authToken = sharedPreferences.getString("jwt_token", null)
                if (authToken.isNullOrBlank()) {
                    _perfilUiState.update { it.copy(isLoading = false, error = "Token de autenticação não encontrado.") }
                    return@launch
                }

                Log.d("PerfilViewModel", "Buscando perfil para usuário $userId")

                val response = withContext(Dispatchers.IO) {
                    perfilApiService.getPerfil(userId, "Bearer $authToken")
                }

                if (response.isSuccessful) {
                    val usuario = response.body()
                    _perfilUiState.update { it.copy(isLoading = false, usuario = usuario) }
                    Log.d("PerfilViewModel", "Perfil carregado com sucesso: ${usuario?.nome}")

                    if (!usuario?.foto_perfil_url.isNullOrBlank()) {
                        Log.d("PerfilViewModel", "Usuário tem URL de foto: ${usuario?.foto_perfil_url}")
                        buscarFotoPerfil(userId)
                    } else {
                        Log.d("PerfilViewModel", "Usuário não tem URL de foto")
                        _profileImageBitmap.value = null
                    }
                } else {
                    val erro = "Erro ao buscar perfil: Código ${response.code()} - ${response.message()}"
                    _perfilUiState.update { it.copy(isLoading = false, error = erro) }
                    Log.e("PerfilViewModel", erro)
                }
            } catch (e: Exception) {
                val erro = "Erro ao buscar perfil: ${e.message}"
                _perfilUiState.update { it.copy(isLoading = false, error = erro) }
                Log.e("PerfilViewModel", "Erro ao buscar perfil", e)
            }
        }
    }

    private fun buscarFotoPerfil(userId: Int) {
        viewModelScope.launch {
            try {
                val authToken = sharedPreferences.getString("jwt_token", null)
                if (authToken.isNullOrBlank()) {
                    Log.e("PerfilViewModel", "Token não encontrado para buscar foto")
                    return@launch
                }

                Log.d("PerfilViewModel", "Buscando foto de perfil para usuário $userId")

                val response = withContext(Dispatchers.IO) {
                    perfilApiService.getFotoPerfil(userId, "Bearer $authToken")
                }

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null) {
                        processarImagemPerfil(responseBody)
                    } else {
                        Log.e("PerfilViewModel", "Corpo da resposta nulo")
                        _profileImageBitmap.value = null
                    }
                } else {
                    Log.e("PerfilViewModel", "Erro ao buscar foto: ${response.code()} - ${response.message()}")
                    _profileImageBitmap.value = null
                }
            } catch (e: Exception) {
                Log.e("PerfilViewModel", "Erro ao buscar foto: ${e.message}", e)
                _profileImageBitmap.value = null
            }
        }
    }

    private suspend fun processarImagemPerfil(responseBody: ResponseBody) {
        withContext(Dispatchers.IO) {
            try {
                val byteArray = responseBody.bytes()

                if (byteArray.isNotEmpty()) {
                    Log.d("PerfilViewModel", "Bytes recebidos: ${byteArray.size}")

                    val tempFile = File(applicationContext.cacheDir, "debug_profile_pic.jpg")
                    tempFile.writeBytes(byteArray)
                    Log.d("PerfilViewModel", "Imagem salva em ${tempFile.absolutePath}")

                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }

                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            Log.d("PerfilViewModel", "Bitmap decodificado com sucesso: ${bitmap.width}x${bitmap.height}")
                            _profileImageBitmap.value = bitmap.asImageBitmap()
                        } else {
                            Log.e("PerfilViewModel", "Falha ao decodificar bitmap")

                            withContext(Dispatchers.IO) {
                                val inputStream = byteArray.inputStream()
                                val alternativeBitmap = BitmapFactory.decodeStream(inputStream)

                                withContext(Dispatchers.Main) {
                                    if (alternativeBitmap != null) {
                                        Log.d("PerfilViewModel", "Bitmap decodificado com abordagem alternativa")
                                        _profileImageBitmap.value = alternativeBitmap.asImageBitmap()
                                    } else {
                                        Log.e("PerfilViewModel", "Todas as tentativas de decodificação falharam")
                                        _profileImageBitmap.value = null
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.e("PerfilViewModel", "Array de bytes vazio")
                    withContext(Dispatchers.Main) {
                        _profileImageBitmap.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e("PerfilViewModel", "Erro ao processar imagem: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _profileImageBitmap.value = null
                }
            }
        }
    }

    fun uploadProfilePicture(userId: Int, imageUri: Uri) {
        viewModelScope.launch {
            _perfilUiState.update { it.copy(isUploading = true, uploadError = null) }

            try {
                val authToken = sharedPreferences.getString("jwt_token", null)
                if (authToken.isNullOrBlank()) {
                    _perfilUiState.update { it.copy(isUploading = false, uploadError = "Token de autenticação não encontrado.") }
                    return@launch
                }

                Log.d("PerfilViewModel", "Iniciando upload de foto para usuário $userId")

                val (tempFile, rotatedBitmap) = withContext(Dispatchers.IO) {
                    val contentResolver = applicationContext.contentResolver
                    val inputStream = contentResolver.openInputStream(imageUri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    val rotationAngle = getRotationFromUri(applicationContext, imageUri).toFloat()
                    val rotatedBitmap = if (rotationAngle != 0f && originalBitmap != null) {
                        rotateBitmap(originalBitmap, rotationAngle)
                    } else {
                        originalBitmap
                    }

                    val tempFile = File(applicationContext.cacheDir, "rotated_profile_pic.jpg")
                    rotatedBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, tempFile.outputStream())

                    Pair(tempFile, rotatedBitmap)
                }

                if (rotatedBitmap != null) {
                    val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                    val response = withContext(Dispatchers.IO) {
                        perfilApiService.uploadFotoPerfil(userId, body, "Bearer $authToken")
                    }

                    if (response.isSuccessful) {
                        _perfilUiState.update { it.copy(isUploading = false, usuario = response.body()) }
                        Log.d("PerfilViewModel", "Upload realizado com sucesso")

                        _profileImageBitmap.value = rotatedBitmap.asImageBitmap()
                        buscarPerfil(userId)
                    } else {
                        val erro = "Erro ao fazer upload da foto: Código ${response.code()} - ${response.message()}"
                        _perfilUiState.update { it.copy(isUploading = false, uploadError = erro) }
                        Log.e("PerfilViewModel", erro)
                    }

                    withContext(Dispatchers.IO) {
                        tempFile.delete()
                    }
                } else {
                    _perfilUiState.update { it.copy(isUploading = false, uploadError = "Erro ao processar a imagem.") }
                }

            } catch (e: Exception) {
                val erro = "Erro ao fazer upload da foto: ${e.message}"
                _perfilUiState.update { it.copy(isUploading = false, uploadError = erro) }
                Log.e("PerfilViewModel", "Erro no upload", e)
            }
        }
    }

    fun deleteProfilePicture(userId: Int) {
        viewModelScope.launch {
            _perfilUiState.update { it.copy(isDeleting = true, deleteError = null) }
            try {
                val authToken = sharedPreferences.getString("jwt_token", null)
                if (authToken.isNullOrBlank()) {
                    _perfilUiState.update { it.copy(isDeleting = false, deleteError = "Token de autenticação não encontrado.") }
                    return@launch
                }

                Log.d("PerfilViewModel", "Deletando foto de perfil para usuário $userId")

                val response = withContext(Dispatchers.IO) {
                    perfilApiService.deleteFotoPerfil(userId, "Bearer $authToken")
                }

                if (response.isSuccessful) {
                    _perfilUiState.update {
                        it.copy(
                            isDeleting = false,
                            usuario = _perfilUiState.value.usuario?.copy(foto_perfil_url = null)
                        )
                    }
                    _profileImageBitmap.value = null
                    Log.d("PerfilViewModel", "Foto deletada com sucesso")
                    buscarPerfil(userId)
                } else {
                    val erro = "Erro ao deletar a foto: Código ${response.code()} - ${response.message()}"
                    _perfilUiState.update { it.copy(isDeleting = false, deleteError = erro) }
                    Log.e("PerfilViewModel", erro)
                }
            } catch (e: Exception) {
                val erro = "Erro ao deletar a foto: ${e.message}"
                _perfilUiState.update { it.copy(isDeleting = false, deleteError = erro) }
                Log.e("PerfilViewModel", "Erro ao deletar foto", e)
            }
        }
    }

    fun buscarFamilia(userId: Int) {
        viewModelScope.launch {
            _perfilUiState.update { it.copy(isFamiliaLoading = true, familiaError = null) }
            try {
                val authToken = sharedPreferences.getString("jwt_token", null)
                if (authToken.isNullOrBlank()) {
                    _perfilUiState.update { it.copy(isFamiliaLoading = false, familiaError = "Token de autenticação não encontrado.") }
                    return@launch
                }

                Log.d("PerfilViewModel", "Buscando família para usuário $userId")

                val response = withContext(Dispatchers.IO) {
                    perfilApiService.getFamilia("Bearer $authToken")
                }

                if (response.isSuccessful) {
                    val familia = response.body()
                    _perfilUiState.update { it.copy(isFamiliaLoading = false, familia = familia) }
                    Log.d("PerfilViewModel", "Família carregada: ${familia?.size} membros")
                } else {
                    val erro = "Erro ao buscar família: Código ${response.code()} - ${response.message()}"
                    _perfilUiState.update { it.copy(isFamiliaLoading = false, familiaError = erro) }
                    Log.e("PerfilViewModel", erro)
                }
            } catch (e: Exception) {
                val erro = "Erro ao buscar família: ${e.message}"
                _perfilUiState.update { it.copy(isFamiliaLoading = false, familiaError = erro) }
                Log.e("PerfilViewModel", "Erro ao buscar família", e)
            }
        }
    }

    fun resetState() {
        _perfilUiState.value = PerfilUiState()
        _profileImageBitmap.value = null
    }

    private suspend fun getRotationFromUri(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        val contentResolver: ContentResolver = context.contentResolver
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(uri)
            val exifInterface = ExifInterface(inputStream!!)
            when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            Log.e("ImageRotation", "Erro ao ler EXIF: ${e.message}")
            0
        } finally {
            inputStream?.close()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationAngle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationAngle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Atualiza a preferência de doação do usuário.
     * Como o backend não suporta atualizações parciais, fazemos a atualização apenas localmente.
     * Em um cenário real, você implementaria um endpoint específico para isso.
     */
    fun atualizarPreferenciasDoacao(userId: Int, receberDoacoes: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("PerfilViewModel", "=== ATUALIZANDO PREFERÊNCIA LOCALMENTE ===")
                Log.d("PerfilViewModel", "UserId: $userId")
                Log.d("PerfilViewModel", "Receber Doações: $receberDoacoes")

                _perfilUiState.update {
                    it.copy(
                        isUpdatingPreferences = true,
                        updatePreferencesError = null,
                        updatePreferencesSuccess = false
                    )
                }

                // Simular um pequeno delay para mostrar o loading
                kotlinx.coroutines.delay(500)

                // Atualizar apenas localmente
                val usuarioAtual = _perfilUiState.value.usuario
                if (usuarioAtual != null) {
                    val usuarioAtualizado = usuarioAtual.copy(receber_doacoes = receberDoacoes)

                    _perfilUiState.update {
                        it.copy(
                            isUpdatingPreferences = false,
                            updatePreferencesSuccess = true,
                            usuario = usuarioAtualizado
                        )
                    }

                    Log.d("PerfilViewModel", "Preferência atualizada localmente com sucesso")

                    // Remover mensagem de sucesso após 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _perfilUiState.update { it.copy(updatePreferencesSuccess = false) }

                } else {
                    _perfilUiState.update {
                        it.copy(
                            isUpdatingPreferences = false,
                            updatePreferencesError = "Dados do usuário não encontrados."
                        )
                    }
                }

            } catch (e: Exception) {
                val erro = "Erro ao atualizar preferências: ${e.message}"
                Log.e("PerfilViewModel", "Exceção: $erro", e)

                _perfilUiState.update {
                    it.copy(
                        isUpdatingPreferences = false,
                        updatePreferencesError = erro
                    )
                }
            }
        }
    }

    /**
     * Versão alternativa que tenta usar o endpoint completo do usuário
     * (descomentada se você quiser tentar novamente com dados completos)
     */
    /*
    fun atualizarPreferenciasDoacao(userId: Int, receberDoacoes: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("PerfilViewModel", "=== TENTANDO ATUALIZAÇÃO COMPLETA ===")

                _perfilUiState.update {
                    it.copy(
                        isUpdatingPreferences = true,
                        updatePreferencesError = null
                    )
                }

                val authToken = sharedPreferences.getString("jwt_token", null)
                if (authToken.isNullOrBlank()) {
                    _perfilUiState.update {
                        it.copy(
                            isUpdatingPreferences = false,
                            updatePreferencesError = "Token de autenticação não encontrado."
                        )
                    }
                    return@launch
                }

                val usuarioAtual = _perfilUiState.value.usuario
                if (usuarioAtual == null) {
                    _perfilUiState.update {
                        it.copy(
                            isUpdatingPreferences = false,
                            updatePreferencesError = "Dados do usuário não encontrados."
                        )
                    }
                    return@launch
                }

                // Enviar o usuário completo com apenas a preferência alterada
                val usuarioParaAtualizar = usuarioAtual.copy(
                    receber_doacoes = receberDoacoes
                )

                Log.d("PerfilViewModel", "Enviando usuário completo...")

                val response = withContext(Dispatchers.IO) {
                    perfilApiService.atualizarPreferencias(
                        id = userId,
                        usuario = usuarioParaAtualizar,
                        authToken = "Bearer $authToken"
                    )
                }

                if (response.isSuccessful) {
                    val usuarioAtualizado = response.body()
                    _perfilUiState.update {
                        it.copy(
                            isUpdatingPreferences = false,
                            updatePreferencesSuccess = true,
                            usuario = usuarioAtualizado ?: usuarioParaAtualizar
                        )
                    }
                    Log.d("PerfilViewModel", "=== ATUALIZAÇÃO CONCLUÍDA COM SUCESSO ===")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val erro = "Erro ${response.code()}: ${response.message()}${if (errorBody != null) " - $errorBody" else ""}"

                    Log.e("PerfilViewModel", "Erro na API: $erro")

                    _perfilUiState.update {
                        it.copy(
                            isUpdatingPreferences = false,
                            updatePreferencesError = erro
                        )
                    }
                }
            } catch (e: Exception) {
                val erro = "Erro ao atualizar preferências: ${e.message}"
                Log.e("PerfilViewModel", "Exceção: $erro", e)

                _perfilUiState.update {
                    it.copy(
                        isUpdatingPreferences = false,
                        updatePreferencesError = erro
                    )
                }
            }
        }
    }
    */
}
