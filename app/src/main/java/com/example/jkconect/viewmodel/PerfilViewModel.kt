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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModelProvider
import com.example.jkconect.data.api.PerfilApiService
import com.example.jkconect.data.api.RetrofitClient
import com.example.jkconect.model.UsuarioResponseDto
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import java.io.File
import java.io.InputStream

data class PerfilUiState(
    val userId: Int? = null,
    val usuario: Usuario? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val familia: List<UsuarioResponseDto>? = null, // Nova propriedade para a família
    val isFamiliaLoading: Boolean = false, // Estado de carregamento da família
    val familiaError: String? = null
)

class PerfilViewModel(
    private val PerfilApiService: PerfilApiService,
    private val sharedPreferences: SharedPreferences,
    private val applicationContext: Context // Para acessar o cacheDir
) : ViewModel(), KoinComponent {

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
                val response = PerfilApiService.getPerfil(userId, "Bearer $authToken")
                if (response.isSuccessful) {
                    _perfilUiState.update { it.copy(isLoading = false, usuario = response.body()) }
                    buscarFotoPerfil(userId)
                } else {
                    _perfilUiState.update { it.copy(isLoading = false, error = "Erro ao buscar perfil: Código ${response.code()}") }
                }
            } catch (e: Exception) {
                _perfilUiState.update { it.copy(isLoading = false, error = "Erro ao buscar perfil: ${e.message}") }
            }
        }
    }

    private fun buscarFotoPerfil(userId: Int) {
        viewModelScope.launch {
            try {
                val authToken = sharedPreferences.getString("jwt_token", null)
                if (!authToken.isNullOrBlank()) {
                    val response = PerfilApiService.getFotoPerfil(userId, "Bearer $authToken")
                    if (response.isSuccessful) {
                        val byteArray = response.body()?.bytes()
                        if (byteArray != null && byteArray.isNotEmpty()) {
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            _profileImageBitmap.update { bitmap?.asImageBitmap() }
                        } else {
                            _profileImageBitmap.update { null }
                        }
                    } else {
                        // Lidar com erros ao buscar a foto de perfil (opcional)
                        println("Erro ao buscar foto de perfil: ${response.code()}")
                        _profileImageBitmap.update { null }
                    }
                }
            } catch (e: Exception) {
                println("Erro ao buscar foto de perfil: ${e.message}")
                _profileImageBitmap.update { null }
            }
        }
    }

   fun uploadProfilePicture(userId: Int, imageUri: Uri) {
       viewModelScope.launch {
           _perfilUiState.update { it.copy(isUploading = true, uploadError = null) }
           var inputStream: InputStream? = null // Declare inputStream fora do try
           try {
               val authToken = sharedPreferences.getString("jwt_token", null)
               if (authToken.isNullOrBlank()) {
                   _perfilUiState.update { it.copy(isUploading = false, uploadError = "Token de autenticação não encontrado.") }
                   return@launch
               }

               val contentResolver = applicationContext.contentResolver
               inputStream = contentResolver.openInputStream(imageUri) // Inicialize inputStream aqui
               val originalBitmap = BitmapFactory.decodeStream(inputStream)

               val rotationAngle = getRotationFromUri(applicationContext, imageUri).toFloat()
               val rotatedBitmap = if (rotationAngle != 0f && originalBitmap != null) {
                   rotateBitmap(originalBitmap, rotationAngle)
               } else {
                   originalBitmap
               }

               if (rotatedBitmap != null) {
                   val tempFile = File(applicationContext.cacheDir, "rotated_profile_pic.jpg")
                   rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, tempFile.outputStream())

                   val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                   val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                   val response = PerfilApiService.uploadFotoPerfil(userId, body, "Bearer $authToken")
                   if (response.isSuccessful) {
                       _perfilUiState.update { it.copy(isUploading = false, usuario = response.body()) }
                       buscarPerfil(userId)
                   } else {
                       _perfilUiState.update { it.copy(isUploading = false, uploadError = "Erro ao fazer upload da foto: Código ${response.code()}") }
                   }
                   tempFile.delete()
               } else {
                   _perfilUiState.update { it.copy(isUploading = false, uploadError = "Erro ao processar a imagem.") }
               }

           } catch (e: Exception) {
               _perfilUiState.update { it.copy(isUploading = false, uploadError = "Erro ao fazer upload da foto: ${e.message}") }
           } finally {
               inputStream?.close()
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
                val response = PerfilApiService.deleteFotoPerfil(userId, "Bearer $authToken")
                if (response.isSuccessful) {
                    _perfilUiState.update { it.copy(isDeleting = false, usuario = _perfilUiState.value.usuario?.copy(foto_perfil_url = null)) }
                    buscarPerfil(userId)
                } else {
                    _perfilUiState.update { it.copy(isDeleting = false, deleteError = "Erro ao deletar a foto: Código ${response.code()}") }
                }
            } catch (e: Exception) {
                _perfilUiState.update { it.copy(isDeleting = false, deleteError = "Erro ao deletar a foto: ${e.message}") }
            }
        }
    }

    fun resetState() {
        _perfilUiState.value = PerfilUiState()
    }


     //ROTAÇÃO IMG

    fun getRotationFromUri(context: Context, uri: Uri): Int {
        val contentResolver: ContentResolver = context.contentResolver
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(uri)
            val exifInterface = ExifInterface(inputStream!!)
            return when (exifInterface.getAttributeInt(
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
            return 0
        } finally {
            inputStream?.close()
        }
    }

    fun rotateBitmap(bitmap: Bitmap, rotationAngle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationAngle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
                val response = PerfilApiService.getFamilia("Bearer $authToken") // Não precisa do userId aqui, pois o backend usa o token
                if (response.isSuccessful) {
                    _perfilUiState.update { it.copy(isFamiliaLoading = false, familia = response.body()) }
                } else {
                    _perfilUiState.update { it.copy(isFamiliaLoading = false, familiaError = "Erro ao buscar família: Código ${response.code()}") }
                }
            } catch (e: Exception) {
                _perfilUiState.update { it.copy(isFamiliaLoading = false, familiaError = "Erro ao buscar família: ${e.message}") }
            }
        }
    }

    class PerfilViewModelFactory(
        private val perfilApiService: PerfilApiService,
        private val sharedPreferences: SharedPreferences,
        private val applicationContext: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PerfilViewModel(perfilApiService, sharedPreferences, applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}


