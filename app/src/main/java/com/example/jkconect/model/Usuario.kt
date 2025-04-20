package com.example.jkconect.model

data class Usuario(
    val nome: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val data_nascimento: String? = null,
    val genero: String? = null,
    val receber_doacoes: Boolean? = null,
    val foto_perfil_url: String? = null,
    val endereco: Endereco? = null
    // Outros campos, se houver
)

data class Endereco(
    val id: Int? = null,
    val cep: String? = null,
    val logradouro: String? = null,
    val numero: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    // Outros campos do endereço, se houver
)
