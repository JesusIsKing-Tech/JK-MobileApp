package com.example.jkconect.navigation.navhost

import HomeScreen
import LoginScreen
import android.content.Context
import android.util.Log
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
fun BottomAppBarNavHost() {
//não serveeeeee
}