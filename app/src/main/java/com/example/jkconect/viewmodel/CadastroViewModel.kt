package com.example.jkconect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jkconect.data.api.EnderecoService
import com.example.jkconect.data.api.PerfilApiService
import com.example.jkconect.model.CadastroUiState
import com.example.jkconect.model.EnderecoCadastroDto
import com.example.jkconect.model.Usuario
import com.example.jkconect.model.UsuarioCadastroDto
import com.example.jkconect.model.UsuarioResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Response

class CadastroViewModel(private val PerfilApiService: PerfilApiService) : ViewModel(), KoinComponent {
    private val _cadastroUiState = MutableStateFlow(CadastroUiState())
    val cadastroUiState: StateFlow<CadastroUiState> = _cadastroUiState.asStateFlow()
    private val enderecoService: EnderecoService by inject()



    fun atualizarNome(nome: String) {
        _cadastroUiState.update { it.copy(nome = nome) }
    }

    fun atualizarEmail(email: String) {
        _cadastroUiState.update { it.copy(email = email, erroConfirmacaoEmail = null) }
    }

    fun atualizarSenha(senha: String) {
        _cadastroUiState.update { it.copy(senha = senha, erroConfirmacaoSenha = null) }
    }

    fun atualizarTelefone(telefone: String) {
        _cadastroUiState.update { it.copy(telefone = telefone) }
    }

    fun atualizarDataNascimento(dataNascimento: String) {
        _cadastroUiState.update { it.copy(dataNascimento = dataNascimento) }
    }

    fun atualizarGenero(genero: String) {
        _cadastroUiState.update { it.copy(genero = genero) }
    }

    fun atualizarReceberDoacoes(receberDoacoes: Boolean?) {
        _cadastroUiState.update { it.copy(receberDoacoes = receberDoacoes) }
    }

    fun atualizarCep(cep: String) {
        _cadastroUiState.update { it.copy(cep = cep) }
    }

    fun atualizarLogradouro(logradouro: String) {
        _cadastroUiState.update { it.copy(logradouro = logradouro) }
    }

    fun atualizarNumero(numero: String) {
        _cadastroUiState.update { it.copy(numero = numero) }
    }

    fun atualizarComplemento(complemento: String?) {
        _cadastroUiState.update { it.copy(complemento = complemento) }
    }

    fun atualizarBairro(bairro: String) {
        _cadastroUiState.update { it.copy(bairro = bairro) }
    }

    fun atualizarLocalidade(localidade: String) {
        _cadastroUiState.update { it.copy(localidade = localidade) }
    }

    fun atualizarUf(uf: String) {
        _cadastroUiState.update { it.copy(uf = uf) }
    }

    fun cadastrar(confirmaEmail: String, confirmaSenha: String, onCadastroSucesso: (Usuario) -> Unit) {
        if (_cadastroUiState.value.email != confirmaEmail) {
            _cadastroUiState.update { it.copy(erroConfirmacaoEmail = "Os emails não coincidem") }
            return
        }
        if (_cadastroUiState.value.senha != confirmaSenha) {
            _cadastroUiState.update { it.copy(erroConfirmacaoSenha = "As senhas não coincidem") }
            return
        }

        viewModelScope.launch {
            _cadastroUiState.update { it.copy(isLoading = true, erro = null, sucesso = null) }
            try {
                val cadastroDto = UsuarioCadastroDto(
                    nome = _cadastroUiState.value.nome,
                    email = _cadastroUiState.value.email,
                    senha = _cadastroUiState.value.senha,
                    telefone = _cadastroUiState.value.telefone,
                    data_nascimento = _cadastroUiState.value.dataNascimento,
                    genero = _cadastroUiState.value.genero,
                    receber_doacoes = _cadastroUiState.value.receberDoacoes ?: false,
                    endereco = EnderecoCadastroDto(
                        cep = _cadastroUiState.value.cep,
                        logradouro = _cadastroUiState.value.logradouro,
                        numero = _cadastroUiState.value.numero,
                        complemento = _cadastroUiState.value.complemento,
                        bairro = _cadastroUiState.value.bairro,
                        localidade = _cadastroUiState.value.localidade,
                        uf = _cadastroUiState.value.uf
                    )
                )
                val response = PerfilApiService.cadastrarUsuario(cadastroDto)
                if (response.isSuccessful) {
                    _cadastroUiState.update { it.copy(isLoading = false, sucesso = response.body()) }
                    response.body()?.let { usuario ->
                        onCadastroSucesso(usuario)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        try {
                            val jsonObject = JSONObject(errorBody)
                            jsonObject.getString("message")
                        } catch (e: JSONException) {
                            "Erro ao cadastrar: Código ${response.code()}"
                        }
                    } else {
                        "Erro ao cadastrar: Código ${response.code()}"
                    }
                    _cadastroUiState.update { it.copy(isLoading = false, erro = errorMessage) }
                }
            } catch (e: Exception) {
                _cadastroUiState.update { it.copy(isLoading = false, erro = "Erro ao cadastrar: ${e.message}") }
            }
        }
    }

    fun buscarEndereco(cep: String) {
        Log.d("CadastroViewModel", "buscarEndereco chamada com CEP: $cep")
        if (cep.length == 8 || cep.length == 9) { // Validação básica do CEP
            _cadastroUiState.update { it.copy(isLoading = true, erro = null) }
            viewModelScope.launch {
                try {
                    Log.d("CadastroViewModel", "Iniciando requisição para o CEP: $cep") // Adicione este log
                    val response = enderecoService.buscarEnderecoPorCep(cep)
                    Log.d("CadastroViewModel", "Resposta da API: $response") // Adicione este log
                    if (response.isSuccessful) {
                        Log.d("CadastroViewModel", "Corpo da resposta: ${response.body()}") // Adicione este log
                        response.body()?.let { endereco ->
                            _cadastroUiState.update {
                                it.copy(
                                    cep = endereco.cep,
                                    logradouro = endereco.logradouro,
                                    bairro = endereco.bairro,
                                    localidade = endereco.localidade,
                                    uf = endereco.uf,
                                    isLoading = false
                                )
                            }
                        }
                    } else {
                        Log.e("CadastroViewModel", "Erro na resposta da API: Código ${response.code()}, Mensagem: ${response.errorBody()?.string()}")
                        _cadastroUiState.update { it.copy(erro = "CEP não encontrado", isLoading = false) }
                    }
                } catch (e: Exception) {
                    Log.e("CadastroViewModel", "Erro na requisição: ${e.message}")
                    _cadastroUiState.update { it.copy(erro = "Erro ao buscar CEP", isLoading = false) }
                } finally {
                    _cadastroUiState.update { it.copy(isLoading = false) }
                }
            }
        } else if (cep.isNotEmpty()) {
            _cadastroUiState.update { it.copy(erro = "CEP inválido") }
        } else {
            _cadastroUiState.update { it.copy(
                cep = "",
                logradouro = "",
                bairro = "",
                localidade = "",
                uf = "",
                erro = null
            ) }
        }

        }
}