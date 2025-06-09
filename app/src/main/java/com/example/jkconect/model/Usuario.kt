package com.example.jkconect.model

data class UsuarioResponseDto(
    val id: Int,
    val nome: String?,
    val email: String?,
    val data_nascimento: String?,
    val telefone: String?,
    // Outras propriedades relevantes
)


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

data class UsuarioCadastroDto(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String,
    val data_nascimento: String, // Formato YYYY-MM-DD
    val genero: String,
    val receber_doacoes: Boolean,
    val endereco: EnderecoCadastroDto
)

data class EnderecoCadastroDto(
    val cep: String,
    val logradouro: String,
    val numero: String,
    val complemento: String?,
    val bairro: String,
    val localidade: String,
    val uf: String
)

data class EnderecoViaCepDTO(
    val cep: String,
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String
)

data class CadastroUiState(
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val telefone: String = "",
    val dataNascimento: String = "",
    val genero: String = "",
    val receberDoacoes: Boolean? = null,
    val cep: String = "",
    val logradouro: String = "",
    val numero: String = "",
    val complemento: String? = null,
    val bairro: String = "",
    val localidade: String = "",
    val uf: String = "",
    val isLoading: Boolean = false,
    val sucesso: com.example.jkconect.model.Usuario? = null, // Use o caminho correto para sua classe Usuario
    val erro: String? = null,
    val erroConfirmacaoEmail: String? = null,
    val erroConfirmacaoSenha: String? = null
)

data class UsuarioAtualizarDto(
    val nome: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val data_nascimento: String? = null,
    val genero: String? = null,
    val receber_doacoes: Boolean? = null,
    val endereco: Endereco? = null
)

