package com.example.jkconect.network.entity
import java.time.LocalDate

  data class Usuario(
    val nome: String,
    val email: String,
    val telefone: String,
    val dataNascimento: LocalDate,
    val genero: String,
    val receberDoacoes: Boolean,
    val fotoPerfilUrl: String
)
