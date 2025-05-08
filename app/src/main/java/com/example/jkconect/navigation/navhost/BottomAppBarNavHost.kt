package com.example.jkconect.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.jkconect.navigation.navhost.MyNavHost


@Composable
fun BottomAppBarNavHost() {
    MaterialTheme {
        Surface {
            val navController = rememberNavController()
            MyNavHost(navHostController = navController)
        }
    }}