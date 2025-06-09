// Arquivo AppModule.kt corrigido
package com.example.jkconect.di

import PedidoDeOracaoViewModel
import android.app.Application
import com.example.jkconect.data.api.AuthInterceptor
import com.example.jkconect.data.api.EnderecoService
import com.example.jkconect.data.api.EventoApiService
import com.example.jkconect.data.api.LoginApiService
import com.example.jkconect.data.api.PerfilApiService
import com.example.jkconect.data.api.UserViewModel
// Importe o RetrofitClient corretamente
import RetrofitClient
import com.example.jkconect.data.api.PedidoOracaoApiService
import com.example.jkconect.data.api.TrocaDeEnderecoApiService
import com.example.jkconect.viewmodel.CadastroViewModel
import com.example.jkconect.viewmodel.EventoUserViewModel
import com.example.jkconect.viewmodel.EventoViewModel
import com.example.jkconect.viewmodel.PerfilViewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import org.koin.androidx.viewmodel.dsl.viewModel
import com.example.jkconect.viewmodel.LoginViewModel
import com.example.jkconect.viewmodel.TrocaEnderecoViewModel
import org.koin.androidx.compose.get
import org.koin.core.scope.get
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    // Interceptores
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    single {
        androidContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    }

    factory {
        AuthInterceptor(get())
    }

    // Cliente HTTP
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<AuthInterceptor>())
            .build()
    }

    // Retrofit
    single {
        RetrofitClient.getInstance(get())
    }

    // Serviços da API
    single { get<Retrofit>().create(PerfilApiService::class.java) }
    single { get<Retrofit>().create(LoginApiService::class.java) }
    single<EnderecoService> { get<Retrofit>().create(EnderecoService::class.java) }
    single { get<Retrofit>().create(EventoApiService::class.java) }
    single<PedidoOracaoApiService> { get<Retrofit>().create(PedidoOracaoApiService::class.java) }

    // CORREÇÃO: Registrar a interface da API corretamente
    single<TrocaDeEnderecoApiService> { get<Retrofit>().create(TrocaDeEnderecoApiService::class.java) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { PerfilViewModel(get(), get(), androidContext()) }
    viewModel { CadastroViewModel(get()) }
    viewModel { EventoViewModel(get(), get()) }
    viewModel { EventoUserViewModel(get(),get(),get()) }


    // Certifique-se de que o UserViewModel está registrado
    // Agora com apenas um parâmetro obrigatório
    single { UserViewModel(get()) }

    // Modificado para injetar o EventoApiService e UserViewModel
    viewModel {
        EventoViewModel(
            api = get(),
            userViewModel = get()
        )
    }

    viewModel {
        EventoUserViewModel(
            api = get(),
            userViewModel = get(),
            eventoViewModel = get()
        )
    }

    viewModel { PedidoDeOracaoViewModel(
        apiService = get())
    }

    // CORREÇÃO: Registrar o ViewModel corretamente
    viewModel { TrocaEnderecoViewModel(
        apiService = get<TrocaDeEnderecoApiService>())
    }
}