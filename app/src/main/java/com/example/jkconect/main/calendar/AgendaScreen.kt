package com.example.jkconect.main.calendar

import Evento
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jkconect.model.EventoUser
import com.example.jkconect.main.home.componentes.EventoDetalhesScreen
import com.example.jkconect.viewmodel.EventoViewModel
import com.example.jkconect.viewmodel.EventoUserViewModel
import com.example.jkconect.data.api.UserViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun CalendarScreen(navController: NavController) {
    // NavController interno para gerenciar as rotas do calendário
    val localNavController = rememberNavController()
    val eventoViewModel: EventoViewModel = getViewModel()
    val eventoUserViewModel: EventoUserViewModel = getViewModel()
    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()

    // Estado para controlar qual tela está ativa
    var selectedScreen by remember { mutableStateOf("Agenda Geral") }

    // Listener para mudanças de rota para atualizar o estado dos botões
    LaunchedEffect(localNavController) {
        localNavController.addOnDestinationChangedListener { _, destination, _ ->
            selectedScreen = when (destination.route) {
                "agenda_geral" -> "Agenda Geral"
                "minha_agenda" -> "Minha Agenda"
                else -> selectedScreen
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Botões de navegação entre Agenda Geral e Minha Agenda
        Row(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    if (selectedScreen != "Agenda Geral") {
                        localNavController.navigate("agenda_geral") {
                            popUpTo("agenda_geral") { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedScreen == "Agenda Geral") Color(0xFF0E48AF) else Color(0xFF888888)
                ),
                modifier = Modifier.size(width = 170.dp, height = 50.dp)
            ) {
                Text(
                    text = "Agenda Geral",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    if (selectedScreen != "Minha Agenda") {
                        localNavController.navigate("minha_agenda") {
                            popUpTo("agenda_geral") { inclusive = false }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedScreen == "Minha Agenda") Color(0xFF0E48AF) else Color(0xFF888888)
                ),
                modifier = Modifier.size(width = 170.dp, height = 50.dp)
            ) {
                Text(
                    text = "Minha Agenda",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // NavHost interno para gerenciar as telas do calendário
        NavHost(
            navController = localNavController,
            startDestination = "agenda_geral",
            modifier = Modifier.fillMaxSize()
        ) {
            // Rota para Agenda Geral
            composable("agenda_geral") {
                AgendaGeral(navController = localNavController)
            }

            // Rota para Minha Agenda
            composable("minha_agenda") {
                MinhaAgendaScreen()
            }

            // Rota para Detalhes do Evento
            composable(
                route = "evento_detalhes/{eventoId}",
                arguments = listOf(navArgument("eventoId") { type = NavType.IntType })
            ) { backStackEntry ->
                val eventoId = backStackEntry.arguments?.getInt("eventoId") ?: return@composable

                val evento = eventoViewModel.eventos.find { it.id == eventoId }
                if (evento != null) {
                    val eventoUser = EventoUser(
                        UsuarioId = userId,
                        EventoId = eventoId,
                        confirmado = eventoUserViewModel.isEventoConfirmado(eventoId),
                        curtir = eventoUserViewModel.isEventoFavoritoFlow(eventoId).collectAsState(initial = false).value
                    )

                    EventoDetalhesScreen(
                        navController = localNavController,
                        evento = evento,
                        eventoUsuario = eventoUser,
                        onFavoritoClick = { evento ->
                            evento.id?.let { eventoId ->
                                eventoUserViewModel.alternarCurtir(userId, eventoId)
                                eventoUserViewModel.carregarEventosCurtidos()
                            }
                        },
                    )
                }
            }
        }
    }
}