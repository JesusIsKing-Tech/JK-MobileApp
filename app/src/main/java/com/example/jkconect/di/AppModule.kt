package com.example.jkconect.di

import android.app.Application
import com.example.jkconect.data.api.ApiService
import com.example.jkconect.data.api.AuthInterceptor
import com.example.jkconect.data.api.LoginApiService
import com.example.jkconect.data.api.RetrofitClient
import com.example.jkconect.viewmodel.PerfilViewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import org.koin.androidx.viewmodel.dsl.viewModel
import com.example.jkconect.viewmodel.LoginViewModel
import org.koin.androidx.compose.get
import org.koin.core.scope.get
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import retrofit2.converter.gson.GsonConverterFactory


const val BASE_URL = "http://192.168.101.13/" // Mantenha sua URL base

val appModule = module {
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single { // Fornece a instância do SharedPreferences
        androidContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    }

    factory { // Cria uma nova instância do AuthInterceptor com o SharedPreferences
        AuthInterceptor(get())
    }

    single { // Fornece a instância do OkHttpClient com os interceptores
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<AuthInterceptor>())
            .build()
    }

    single { // Fornece a instância do Retrofit usando o OkHttpClient configurado
        RetrofitClient.getInstance(get())
    }

    single { get<Retrofit>().create(ApiService::class.java) }
    single { get<Retrofit>().create(LoginApiService::class.java) }

    viewModel { LoginViewModel(get()) }
    viewModel { PerfilViewModel(get(), get()) }
}

