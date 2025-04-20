package com.example.jkconect.model


data class LoginResponse(
    val userId: Int? = null,
    val nome: String? = null,
    val email: String? = null,
    val token: String? = null
)