package com.example.jkconect.navigation.navhost

import HomeScreen
import LoginScreen
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jkconect.main.calendar.CalendarScreen
import com.example.jkconect.main.feed.FeedScreen
import com.example.jkconect.main.myevents.MyEvents
import com.example.jkconect.main.profile.ProfileScreen
import com.example.jkconect.navigation.item.CalendarScreenRoute
import com.example.jkconect.navigation.item.FeedScreenRoute
import com.example.jkconect.navigation.item.HomeScreenRoute
import com.example.jkconect.navigation.item.MyEventsScreenRoute
import com.example.jkconect.viewmodel.PerfilViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun BottomAppBarNavHost(navHostController: NavHostController ) {
    NavHost(
        navController = navHostController,
        startDestination = HomeScreenRoute.route
    ) {
        composable(HomeScreenRoute.route) {
            HomeScreen()
        }

        composable(CalendarScreenRoute.route) {
            CalendarScreen()
        }

        composable(FeedScreenRoute.route) {
            FeedScreen()
        }

        composable(MyEventsScreenRoute.route) {
            MyEvents()
        }

        composable("profile/{userId}") { backStackEntry ->
            val userIdString = backStackEntry.arguments?.getString("userId")
            if (!userIdString.isNullOrEmpty()) {
                val userId = userIdString.toIntOrNull()
                if (userId != null) {
                    val perfilViewModel: PerfilViewModel = koinViewModel { parametersOf(userId) }
                    ProfileScreen(perfilViewModel = perfilViewModel)
                } else {
                    Text("ID de usuário inválido")
                }
            } else {
                Text("ID de usuário não fornecido")
            }
        }
    }
}