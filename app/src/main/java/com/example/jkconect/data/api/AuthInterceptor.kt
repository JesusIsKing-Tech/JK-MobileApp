package com.example.jkconect.data.api

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class AuthInterceptor(private val sharedPreferences: SharedPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authToken = sharedPreferences.getString("jwt_token", null)

        // Log para debug
        Log.d("AuthInterceptor", "Token: ${authToken?.take(10)}...")

        val authenticatedRequest = originalRequest.newBuilder()
            .apply {
                if (!authToken.isNullOrBlank()) {
                    header("Authorization", "Bearer $authToken")
                    Log.d("AuthInterceptor", "Adicionando token de autenticação à requisição: ${originalRequest.url}")
                } else {
                    Log.d("AuthInterceptor", "Requisição sem token: ${originalRequest.url}")
                }
            }
            .build()
        return chain.proceed(authenticatedRequest)
    }
}

// ViewModel para armazenar informações globais do usuário, como o token e userId
class UserViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    // Token de autenticação
    private val _authToken = MutableStateFlow(sharedPreferences.getString("jwt_token", "") ?: "")
    val authToken: StateFlow<String> = _authToken.asStateFlow()

    // ID do usuário
    private val _userId = MutableStateFlow(sharedPreferences.getInt("user_id", -1))
    val userId: StateFlow<Int> = _userId.asStateFlow()

    init {
        Log.d("UserViewModel", "Inicializado com userId: ${_userId.value}, token: ${_authToken.value.take(10)}...")
    }

    fun updateAuthToken(newToken: String) {
        _authToken.value = newToken
        sharedPreferences.edit().putString("jwt_token", newToken).apply()
        Log.d("UserViewModel", "Token atualizado: ${newToken.take(10)}...")
    }

    fun updateUserId(newUserId: Int) {
        _userId.value = newUserId
        sharedPreferences.edit().putInt("user_id", newUserId).apply()
        Log.d("UserViewModel", "UserId atualizado: $newUserId")
    }

    fun clearUserData() {
        viewModelScope.launch {
            // Limpar SharedPreferences
            sharedPreferences.edit().apply {
                remove("jwt_token")
                remove("user_id")
                apply()
            }

            // Resetar estados
            _authToken.value = ""
            _userId.value = -1

            Log.d("UserViewModel", "Dados do usuário limpos")
        }
    }

    fun isLoggedIn(): Boolean {
        return _userId.value != -1 && !_authToken.value.isBlank()
    }
}

val userViewModelModule = module {
    viewModel { UserViewModel(get()) }
}
