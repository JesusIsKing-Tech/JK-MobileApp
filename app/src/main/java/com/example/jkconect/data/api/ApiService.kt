package com.example.jkconect.data.api

import com.example.jkconect.model.Usuario
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header

interface ApiService {
    @GET("usuarios/{id}")
    suspend fun getPerfil(
        @Path("id") id: Int,
        @Header("Authorization") authToken: String
    ): Response<Usuario>
}


data class LoginRequest(val email: String, val senha: String)
data class LoginResponse(val token: String, val userId: Int) // Adapte a resposta do seu backend

interface LoginApiService {
    @POST("usuarios/login") // Substitua pelo seu endpoint de login
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}


