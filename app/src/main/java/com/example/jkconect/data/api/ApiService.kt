package com.example.jkconect.data.api

import com.example.jkconect.model.EnderecoViaCepDTO
import com.example.jkconect.model.Usuario
import com.example.jkconect.model.UsuarioCadastroDto
import com.example.jkconect.model.UsuarioResponseDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Streaming

interface PerfilApiService {
    @GET("usuarios/{id}")
    suspend fun getPerfil(
        @Path("id") id: Int,
        @Header("Authorization") authToken: String
    ): Response<Usuario>

    @Multipart
    @POST("usuarios/cadastrar/foto-perfil/{id}")
    suspend fun uploadFotoPerfil(
        @Path("id") userId: Int,
        @Part file: MultipartBody.Part,
        @Header("Authorization") authToken: String
    ): Response<Usuario>

    @DELETE("usuarios/deletar-foto-perfil/{id}")
    suspend fun deleteFotoPerfil(
        @Path("id") userId: Int,
        @Header("Authorization") authToken: String
    ): Response<Unit>

    @GET("usuarios/{id}/foto-perfil")
    @Streaming // Importante para arquivos grandes
    suspend fun getFotoPerfil(
        @Path("id") userId: Int,
        @Header("Authorization") authToken: String
    ): Response<ResponseBody>

    @GET("usuarios/familia")
    suspend fun getFamilia(
        @Header("Authorization") authToken: String
    ): Response<List<UsuarioResponseDto>>


    @POST("usuarios/cadastrar")
    suspend fun cadastrarUsuario(@Body usuario: UsuarioCadastroDto): Response<Usuario>
}


interface EnderecoService {
    @GET("enderecos/buscar/{cep}")
    suspend fun buscarEnderecoPorCep(@Path("cep") cep: String): Response<EnderecoViaCepDTO>
}



data class LoginRequest(val email: String, val senha: String)
data class LoginResponse(val token: String, val userId: Int)

interface LoginApiService {
    @POST("usuarios/login") // Substitua pelo seu endpoint de login
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}


