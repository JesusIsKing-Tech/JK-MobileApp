package com.example.jkconect.navigation.navhost

import CadastroScreen
import LoginScreen
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.main.calendar.CalendarScreen
import com.example.jkconect.main.home.HomeScreenNavigation
import com.example.jkconect.main.myevents.MyEventsScreen
import com.example.jkconect.main.profile.ProfileScreen
import com.example.jkconect.navigation.item.CalendarScreenRoute
import com.example.jkconect.navigation.item.HomeScreenRoute
import com.example.jkconect.navigation.item.LoginScreenRoute
import com.example.jkconect.navigation.item.MyEventsScreenRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyNavHost(navHostController: NavHostController, modifier: Modifier = Modifier) {
    // Obter o UserViewModel para verificar o estado de autenticação
    val userViewModel: UserViewModel = koinViewModel()
    val userId by userViewModel.userId.collectAsState()

    NavHost(
        navController = navHostController,
        startDestination = LoginScreenRoute.route,
        modifier = modifier
    ) {
        composable(LoginScreenRoute.route) {
            LoginScreen(navController = navHostController, onLoginSuccess = { token, userId ->
                Log.d("MyNavHost", "Login bem-sucedido, Token: ${token.take(10)}..., UserId: $userId")
                userViewModel.updateAuthToken(token)
                userViewModel.updateUserId(userId)
                navHostController.navigate(HomeScreenRoute.route) {
                    popUpTo(LoginScreenRoute.route) { inclusive = true }
                }
            })
        }

        composable(HomeScreenRoute.route) {
            Log.d(TAG, "Navegando para a tela inicial")
            HomeScreenNavigation()
        }

        composable(CalendarScreenRoute.route) {
            Log.d(TAG, "Navegando para a tela de calendário")
            CalendarScreen(navController = navHostController)
        }

        composable(MyEventsScreenRoute.route) {
            Log.d(TAG, "Navegando para a tela de eventos favoritos")
            MyEventsScreen()
        }

        // Rota para perfil com userId dinâmico
        composable("profile/{userId}") { backStackEntry ->
            val profileUserId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: userId

            if (profileUserId != -1) {
                Log.d("MyNavHost", "Navegando para a tela de perfil com userId: $profileUserId")

                ProfileScreen(
                    userId = profileUserId,
                    onLogout = {
                        Log.d("MyNavHost", "Logout solicitado")

                        // Limpar dados do usuário
                        userViewModel.clearUserData()

                        // Navegar para tela de login e limpar stack
                        navHostController.navigate(LoginScreenRoute.route) {
                            popUpTo(0) { inclusive = true } // Remove todas as telas do stack
                        }
                    }
                )
            } else {
                Text("ID de usuário inválido")

                // Redirecionar para login se não houver ID válido
                navHostController.navigate(LoginScreenRoute.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Rota para perfil sem parâmetro (usa o userId do ViewModel)
        composable("profile") {
            if (userId != -1) {
                Log.d("MyNavHost", "Navegando para a tela de perfil com userId do ViewModel: $userId")

                ProfileScreen(
                    userId = userId,
                    onLogout = {
                        Log.d("MyNavHost", "Logout solicitado")

                        // Limpar dados do usuário
                        userViewModel.clearUserData()

                        // Navegar para tela de login e limpar stack
                        navHostController.navigate(LoginScreenRoute.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            } else {
                Text("Usuário não logado")

                // Redirecionar para login se não houver usuário logado
                navHostController.navigate(LoginScreenRoute.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable("cadastro") {
            CadastroScreen(
                onCadastroSucesso = { usuario ->
                    Log.d("MyNavHost", "Cadastro bem-sucedido para o usuário: ${usuario.nome}")
                    navHostController.popBackStack() // Volta para a tela de login após o cadastro
                },
                onVoltarClick = {
                    navHostController.popBackStack() // Volta para a tela anterior (login)
                }
            )
        }
    }
}

// Extensões úteis para navegação
fun NavHostController.navigateToProfile(userId: Int? = null) {
    if (userId != null) {
        navigate("profile/$userId")
    } else {
        navigate("profile")
    }
}

fun NavHostController.navigateToLogin() {
    navigate(LoginScreenRoute.route) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavHostController.logout() {
    navigate(LoginScreenRoute.route) {
        popUpTo(0) { inclusive = true }
    }
}
