package com.example.jkconect.navigation.navhost

import CadastroScreen
import LoginScreen
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.main.calendar.CalendarScreen
import com.example.jkconect.main.home.HomeScreen
import com.example.jkconect.main.home.HomeScreenNavigation
import com.example.jkconect.main.myevents.MyEventsScreen
import com.example.jkconect.main.profile.ProfileScreen
import com.example.jkconect.navigation.item.CalendarScreenRoute
import com.example.jkconect.navigation.item.HomeScreenRoute
import com.example.jkconect.navigation.item.LoginScreenRoute
import com.example.jkconect.navigation.item.MyEventsScreenRoute
import com.example.jkconect.viewmodel.PerfilViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun MyNavHost(navHostController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navHostController,
        startDestination = LoginScreenRoute.route,
        modifier = modifier
    ) {
        composable(LoginScreenRoute.route) {
            val userViewModel: UserViewModel = koinViewModel()
           LoginScreen(navController = navHostController, onLoginSuccess = { token, userId ->
                Log.d("MyNavHost", "Login bem-sucedido, Token: $token, UserId: $userId")
               userViewModel.updateAuthToken(token)
                userViewModel.updateUserId(userId)
                navHostController.navigate("profile/$userId") {
                    popUpTo(LoginScreenRoute.route) { inclusive = true }
                }
           })
       }
        composable(HomeScreenRoute.route) {
            Log.d(TAG, "Navegando para a tela inicial")
            HomeScreenNavigation() // Usando a versão atualizada com navegação interna
        }

        composable(CalendarScreenRoute.route) {
            Log.d(TAG, "Navegando para a tela de calendário")
            CalendarScreen()
        }

        composable(MyEventsScreenRoute.route) {
            Log.d(TAG, "Navegando para a tela de eventos favoritos")
            MyEventsScreen() // Usando o nome correto da função
        }
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            if (userId != null) {
                Log.d("MyNavHost", "Navegando para a tela de perfil com userId: $userId")
                val perfilViewModel: PerfilViewModel = koinViewModel()
                ProfileScreen(perfilViewModel = perfilViewModel, userId = userId)
            } else {
                Text("ID de usuário inválido")
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




