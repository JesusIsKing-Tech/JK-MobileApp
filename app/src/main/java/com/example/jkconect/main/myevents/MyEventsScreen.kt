// Correção para MyEventsScreen
package com.example.jkconect.main.myevents

import Evento
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.main.home.componentes.EventoCardHorizontal
import com.example.jkconect.main.home.componentes.EventoDetalhesScreen
import com.example.jkconect.model.EventoUser
import com.example.jkconect.viewmodel.EventoViewModel
import org.koin.androidx.compose.getViewModel

private const val TAG = "MyEventsScreen"
const val EVENTO_DETALHES_ROUTE_FAVORITOS = "evento_detalhes_favoritos/{eventoId}"
const val TODOS_EVENTOS_CURTIDOS_ROUTE = "todos_eventos_curtidos"

@Composable
fun MyEventsScreen() {
    Log.d(TAG, "Iniciando MyEventsScreen")
    MyEventsNavigation()
}

@Composable
fun MyEventsNavigation() {
    val navController = rememberNavController()
    val viewModel: EventoViewModel = getViewModel()
    val eventos = viewModel.eventos
    val isLoading by viewModel.isLoading.collectAsState()
    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()
    val eventosFavoritos by viewModel.eventosFavoritos.collectAsState()

    // Carregar eventos quando a tela for iniciada
    LaunchedEffect(Unit) {
        Log.d(TAG, "Carregando eventos para MyEventsScreen")
        viewModel.carregarEventos()
        viewModel.carregarFavoritos() // Certifique-se de que esta função está sendo chamada
    }

    NavHost(
        navController = navController,
        startDestination = TODOS_EVENTOS_CURTIDOS_ROUTE
    ) {
        composable(
            EVENTO_DETALHES_ROUTE_FAVORITOS,
            arguments = listOf(navArgument("eventoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId")?.toIntOrNull()
            Log.d(TAG, "Navegando para detalhes do evento favorito ID: $eventoId")

            val evento = eventos.find { it.id == eventoId }

            if (evento != null) {
                Log.d(TAG, "Evento favorito encontrado: ${evento.titulo}")
                val eventoUsuario = EventoUser(
                    UsuarioId = userId,
                    EventoId = eventoId ?: 0,
                    confirmado = false,
                    curtir = true // Já é um favorito
                )

                EventoDetalhesScreen(
                    navController = navController,
                    evento = evento,
                    eventoUsuario = eventoUsuario,
                    onFavoritoClick = {
//                        eventoId?.let { viewModel.alternarFavorito(it) }
                    }
                )
            } else {
                Log.e(TAG, "Evento favorito não encontrado para ID: $eventoId")
                // Exibir mensagem de erro ou redirecionar para a tela inicial
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Evento não encontrado",
                            fontSize = 20.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF3B5FE9), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        composable(TODOS_EVENTOS_CURTIDOS_ROUTE) {
            TodosEventosCurtidosScreen(
                navController = navController,
                eventos = eventos,
                isLoading = isLoading,
                eventosFavoritos = eventosFavoritos,
                onFavoritoClick = { evento ->
//                    evento.id?.let { viewModel.alternarFavorito(it) }
                }
            )
        }
    }
}

@Composable
fun TodosEventosCurtidosScreen(
    navController: NavController,
    eventos: List<Evento>,
    isLoading: Boolean,
    eventosFavoritos: List<Int>,
    onFavoritoClick: (Evento) -> Unit
) {
    Log.d(TAG, "Renderizando TodosEventosCurtidosScreen com ${eventos.size} eventos e ${eventosFavoritos.size} favoritos")

    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()

    // Filtrar apenas eventos curtidos
    val eventosCurtidos = remember(eventos, eventosFavoritos) {
        Log.d(TAG, "Filtrando eventos curtidos. IDs favoritos: $eventosFavoritos")
        eventos.filter { evento ->
            val isFavorito = evento.id?.let { id -> eventosFavoritos.contains(id) } ?: false
            Log.d(TAG, "Evento ${evento.id} é favorito: $isFavorito")
            isFavorito
        }
    }

    // Cores
    val backgroundColor = Color(0xFF121212)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val primaryColor = Color(0xFF3B5FE9)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 30.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Meus Eventos Favoritos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicador de carregamento
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Carregando seus eventos favoritos...",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Lista de eventos curtidos
            else if (eventosCurtidos.isEmpty()) {
                // Mensagem quando não há eventos curtidos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Você ainda não curtiu nenhum evento",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Explore eventos e marque seus favoritos",
                            fontSize = 14.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(eventosCurtidos) { evento ->
                        Log.d(TAG, "Renderizando evento curtido: ${evento.id}, ${evento.titulo}")

                        val eventoUser = EventoUser(
                            UsuarioId = userId,
                            EventoId = evento.id ?: 0,
                            confirmado = false,
                            curtir = true // Já é um favorito
                        )

                        EventoCardHorizontal(
                            evento = evento,
                            eventoUsuario = eventoUser,
                            onFavoritoClick = {
                                Log.d(TAG, "Removendo favorito para evento ${evento.id}")
                                onFavoritoClick(evento)
                            },
                            onClick = {
                                Log.d(TAG, "Navegando para detalhes do evento favorito ${evento.id}")
                                navController.navigate("evento_detalhes_favoritos/${evento.id}")
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}