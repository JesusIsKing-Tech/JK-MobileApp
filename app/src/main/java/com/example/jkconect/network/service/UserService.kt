package com.example.jkconect.network.service

import com.example.jkconect.network.models.LoginRequest
import com.example.jkconect.network.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST("usuarios/login")
    suspend fun getAuthentication(@Body request: LoginRequest): Response<LoginResponse>
}