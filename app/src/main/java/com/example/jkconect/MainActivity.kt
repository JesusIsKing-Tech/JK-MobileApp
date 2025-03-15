package com.example.jkconect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.navigation.compose.rememberNavController
import com.example.jkconect.navigation.navhost.MyNavHost
import com.example.jkconect.ui.theme.JKConectTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge() permite que o app ocupe toda a tela
        setContent {
            JKConectTheme {
                MyNavHost(navHostController = rememberNavController())
            }
        }
    }

}

