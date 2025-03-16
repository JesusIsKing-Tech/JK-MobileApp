package com.example.jkconect.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jkconect.main.calendar.CalendarScreen
import com.example.jkconect.main.feed.FeedScreen
import com.example.jkconect.main.myevents.MyEvents
import com.example.jkconect.main.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Composable
fun BottomAppBarNavHost(navHostController: NavHostController ) {
    NavHost(
        navController = navHostController,
        startDestination = FeedScreenRoute
    ){
        composable<FeedScreenRoute> {
            FeedScreen()
        }

        composable<CalendarScreenRoute> {
            CalendarScreen()
        }

        composable<MyEventsScreenRoute> {
            MyEvents()
        }

        composable<ProfileScreenRoute> {
            ProfileScreen()
        }
    }
}

@Serializable
object FeedScreenRoute

@Serializable
object CalendarScreenRoute

@Serializable
object MyEventsScreenRoute

@Serializable
object ProfileScreenRoute