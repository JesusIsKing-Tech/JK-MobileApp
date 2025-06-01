// Arquivo AuthInterceptor.kt corrigido
package com.example.jkconect.data.api

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class AuthInterceptor(private val sharedPreferences: SharedPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authToken = sharedPreferences.getString("jwt_token", null)

        val authenticatedRequest = originalRequest.newBuilder()
            .apply {
                if (authToken != null) {
                    header("Authorization", "Bearer $authToken")
                }
            }
            .build()
        return chain.proceed(authenticatedRequest)
    }
}

// ViewModel para armazenar informações globais do usuário, como o token e userId
class UserViewModel(
    private val sharedPreferences: SharedPreferences,
    private val loginApiService: LoginApiService? = null // Tornando opcional para compatibilidade
) : ViewModel() {
    val authToken = mutableStateOf(sharedPreferences.getString("jwt_token", "") ?: "")
    private val _userId = MutableStateFlow(sharedPreferences.getInt("userId", -1))
    val userId: StateFlow<Int> = _userId.asStateFlow()

    fun updateAuthToken(newToken: String) {
        authToken.value = newToken
        sharedPreferences.edit().putString("jwt_token", newToken).apply()
    }

    fun updateUserId(newUserId: Int) {
        _userId.value = newUserId
        sharedPreferences.edit().putInt("userId", newUserId).apply()
    }
}

val userViewModelModule = module {
    viewModel { UserViewModel(get()) }
}