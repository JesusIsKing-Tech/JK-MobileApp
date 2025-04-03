package com.example.jkconect.network.models

import com.example.jkconect.network.entity.Usuario

data class LoginResponse(
    val token: String,
    val usuarioId: Int,
    val usuario: Usuario
)