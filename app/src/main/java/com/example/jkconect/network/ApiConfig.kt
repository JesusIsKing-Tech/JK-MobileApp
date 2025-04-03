package com.example.kotlinbackend.conexaoBack

                import androidx.compose.runtime.mutableStateListOf
                import androidx.lifecycle.ViewModel
                import androidx.lifecycle.viewModelScope
                import kotlinx.coroutines.launch
                import retrofit2.Retrofit
                import retrofit2.converter.gson.GsonConverterFactory
                import okhttp3.OkHttpClient
                import okhttp3.logging.HttpLoggingInterceptor
                import android.util.Log
                import com.example.jkconect.network.entity.Usuario
                import com.example.jkconect.network.models.LoginRequest
                import com.example.jkconect.network.service.UserService
                import com.example.jkconect.network.models.LoginResponse

                class ApiConfig : ViewModel() {

                    private val userService: UserService
                    var User = Usuario::class

                    private val apiResponses = mutableStateListOf<LoginResponse>()

                    init {
                        val logging = HttpLoggingInterceptor().apply {
                            setLevel(HttpLoggingInterceptor.Level.BODY)
                        }

                        val client = OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .build()

                        val retrofit = Retrofit.Builder()
                            .baseUrl("https://localhost:80/")
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        userService = retrofit.create(UserService::class.java)
                    }

                    fun fetchLogin(email: String, senha: String) {
                        viewModelScope.launch {
                            try {
                                val response = userService.getAuthentication(LoginRequest(email, senha))
                                apiResponses.clear()
                                response.body()?.let { apiResponses.add(it) }
                                Log.d("API_RESPONSE", "login pok")
                            } catch (e: Exception) {
                                Log.e("API_ERROR", "Error fetching data", e)
                                // Você pode adicionar um estado para mostrar o erro na UI
                            }
                        }
                    }
                }