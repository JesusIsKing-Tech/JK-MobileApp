package com.example.jkconect.main.calendar

import Evento
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.jkconect.viewmodel.EventoUserViewModel
import com.example.jkconect.viewmodel.EventoViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

const val EVENTO_DETALHES_ROUTE_PRESENCA = "evento_detalhes_presenca/{eventoId}"
const val TODOS_EVENTOS_CONFIRMADOS_ROUTE = "todos_eventos_confirmados"

@Composable
fun MinhaAgendaScreen() {
    Log.d(TAG, "Iniciando MinhaAgendaScreen")
    MinhaAgendaNavigation()
}

@Composable
fun MinhaAgendaNavigation() {
    val navController = rememberNavController()
    val eventoUserViewModel: EventoUserViewModel = getViewModel()
    val userViewModel: UserViewModel = getViewModel()
    val eventoViewModel: EventoViewModel = getViewModel()

    // Usar collectAsState para observar o StateFlow
    val eventosConfirmados by eventoUserViewModel.eventosConfirmadosCompletos.collectAsState()
    val isLoading by eventoUserViewModel.isLoading.collectAsState()
    val userId by userViewModel.userId.collectAsState()

    // Efeito para carregar eventos quando a tela for iniciada
    LaunchedEffect(Unit) {
        Log.d(TAG, "Carregando eventos para MinhaAgendaNavigation")
        eventoViewModel.carregarEventos()
        eventoUserViewModel.carregarEventosConfirmados()
    }

    // Efeito para recarregar quando houver mudanças nos eventos confirmados
    LaunchedEffect(eventoUserViewModel.eventosConfirmadosCompletos) {
        Log.d(TAG, "Lista de eventos confirmados atualizada: ${eventoUserViewModel.eventosConfirmadosCompletos.value.size} eventos")
        // Não precisamos recarregar do backend aqui, pois já estamos observando a lista atualizada
    }



    NavHost(
        navController = navController,
        startDestination = TODOS_EVENTOS_CONFIRMADOS_ROUTE
    ) {
        composable(
            EVENTO_DETALHES_ROUTE_PRESENCA,
            arguments = listOf(navArgument("eventoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId")?.toIntOrNull()
            Log.d(TAG, "Navegando para detalhes do evento com presença ID: $eventoId")

            val evento = eventosConfirmados.find { it.id == eventoId }

            if (evento != null) {
                Log.d(TAG, "Evento com presença encontrado: ${evento.titulo}")
                val eventoUsuario = EventoUser(
                    UsuarioId = userId,
                    EventoId = eventoId ?: 0,
                    confirmado = true, // Já está confirmado
                    curtir = eventoUserViewModel.isEventoFavoritoFlow(eventoId ?: 0).collectAsState(initial = false).value
                )

                EventoDetalhesScreen(
                    navController = navController,
                    evento = evento,
                    eventoUsuario = eventoUsuario,
                    onFavoritoClick = { evento ->
                        evento.id?.let { eventoId ->
                            eventoUserViewModel.alternarCurtir(userId, eventoId)
                        }
                    },
                )
            } else {
                Log.e(TAG, "Evento com presença não encontrado para ID: $eventoId")
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
                            text = "Evento removido da sua agenda.",
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
        composable(TODOS_EVENTOS_CONFIRMADOS_ROUTE) {
            EventosConfirmadosScreen(
                navController = navController,
                isLoading = isLoading,
                onCancelarPresencaClick = { evento ->
                    evento.id?.let { eventoId ->
                        eventoUserViewModel.cancelarPresenca(userId, eventoId)
                        // Recarregar eventos após cancelar presença
                        eventoUserViewModel.carregarEventosConfirmados()
                    }
                },
            )
        }
    }
}

@Composable
fun EventosConfirmadosScreen(
    navController: NavController,
    isLoading: Boolean,
    onCancelarPresencaClick: (Evento) -> Unit
) {
    val eventoUserViewModel: EventoUserViewModel = getViewModel()
    val eventosConfirmados by eventoUserViewModel.eventosConfirmadosCompletos.collectAsState()

    Log.d(TAG, "Renderizando EventosConfirmadosScreen com ${eventosConfirmados.size} eventos confirmados")

    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Observar mensagens de erro e sucesso
    val errorMessage by eventoUserViewModel.errorMessage.collectAsState()
    val successMessage by eventoUserViewModel.successMessage.collectAsState()

    // Recarregar eventos quando a navegação mudar
    LaunchedEffect(navController.currentBackStackEntry) {
        Log.d(TAG, "MinhaAgendaNavigation - Recarregando eventos confirmados após navegação")
        eventoUserViewModel.carregarEventosConfirmados()
    }




    // Mostrar mensagens de erro ou sucesso
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            eventoUserViewModel.limparErro()
        }

        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            eventoUserViewModel.limparSucesso()
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
                    text = "Meus eventos confirmados",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            WhiteDivider()

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
                            text = "Carregando sua agenda...",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Lista de eventos confirmados
            else if (eventosConfirmados.isEmpty()) {
                // Mensagem quando não há eventos confirmados
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
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Você ainda não confirmou presença em nenhum evento",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Explore eventos e confirme sua presença",
                            fontSize = 14.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(eventosConfirmados) { evento ->
                        Log.d(TAG, "Renderizando evento na lista: ${evento.id}, ${evento.titulo}")

                        EventoCardHorizontal(
                            evento = evento,
                            onFavoritoClick = {
                                Log.d(TAG, "Favorito clicado para evento ${evento.id}")
                                evento.id?.let { eventoId ->
                                    eventoUserViewModel.alternarCurtir(userId, eventoId)
                                }
                            },
                            onClick = {
                                Log.d(TAG, "Navegando para detalhes do evento ${evento.id}")
                                navController.navigate("evento_detalhes_presenca/${evento.id}")
                            }
                        )
                    }

                    // Espaço no final da lista
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
        // SnackbarHost para exibir notificações
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun WhiteDivider() {
    // Exemplo:
     Divider(color = Color.White, thickness = 1.dp)
    // Ou use um Box com altura e cor de fundo

}
