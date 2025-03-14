package com.example.jkconect.navigation.navhost

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Composable
fun MyNavHost(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = HomeScreenRoute
    ){
        composable<HomeScreenRoute> {
            HomeScreen()
        }
    }
    
}

@Serializable
object HomeScreenRoute