package com.example.jkconect

import HomeScreen
import LoginScreen
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.compose.rememberNavController
import com.example.jkconect.di.appModule
import com.example.jkconect.main.calendar.CalendarScreen
import com.example.jkconect.main.feed.FeedScreen
import com.example.jkconect.main.myevents.MyEvents
import com.example.jkconect.main.profile.ProfileScreen
import com.example.jkconect.navigation.navhost.MyNavHost
import com.example.jkconect.ui.theme.JKConectTheme
import com.example.jkconect.viewmodel.PerfilViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.parameter.parametersOf
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.data.api.userViewModelModule
import com.example.jkconect.navigation.item.BottomNavItem
import com.example.jkconect.navigation.item.CalendarScreenRoute
import com.example.jkconect.navigation.item.FeedScreenRoute
import com.example.jkconect.navigation.item.HomeScreenRoute
import com.example.jkconect.navigation.item.MyEventsScreenRoute
import com.example.jkconect.navigation.item.ProfileScreenRoute
import kotlinx.coroutines.flow.collectLatest


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin {
            androidContext(this@MainActivity)
            modules(appModule, userViewModelModule)
        }
        setContent {
            JKConectTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        val profileRouteBase = "profile/"
                        if (currentRoute == HomeScreenRoute.route ||
                            currentRoute == CalendarScreenRoute.route ||
                            currentRoute == FeedScreenRoute.route ||
                            currentRoute == MyEventsScreenRoute.route ||
                            currentRoute == ProfileScreenRoute.route ||
                            currentRoute?.startsWith(profileRouteBase) == true
                        ) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    MyNavHost(
                        navHostController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}



// ESTILIZAR A NAV
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userViewModel: UserViewModel = koinViewModel()
    val loggedInUserId by userViewModel.userId.collectAsState(initial = -1)

    BottomNavigation(
        backgroundColor = Color(0xFF1C1D21),
        contentColor = Color.White,
        elevation = 6.dp,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 6.dp).clip(RoundedCornerShape(20.dp))
    ) {
        BottomNavItem.items.forEach { item ->
            val selected = currentRoute?.startsWith(item.route) == true || currentRoute == item.route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = {
                        if (item is BottomNavItem.Profile) {
                            if (loggedInUserId != -1) {
                                navController.navigate("profile/$loggedInUserId") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                Log.e("BottomNav", "UserId não disponível para navegar ao perfil.")
                            }
                        } else if (item.route != currentRoute) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (selected) item.selectedIcon else item.unselectedIcon
                        ),
                        contentDescription = stringResource(item.title),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
            }
        }
    }
}