package com.example.jkconect.main.home

import Evento
import IgrejaChatComponent
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jkconect.data.api.PerfilApiService
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.main.home.componentes.EventoCard
import com.example.jkconect.main.home.componentes.EventoDetalhesScreen
import com.example.jkconect.main.home.componentes.TodosEventosScreen
import com.example.jkconect.model.EventoUser
import com.example.jkconect.model.Usuario
import com.example.jkconect.viewmodel.CadastroViewModel
import com.example.jkconect.viewmodel.EventoUserViewModel
import com.example.jkconect.viewmodel.EventoViewModel
import com.example.jkconect.viewmodel.PerfilViewModel
import com.example.jkconect.viewmodel.TrocaEnderecoViewModel
import org.json.JSONObject
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.viewModel
import java.net.HttpURLConnection
import java.net.URL

// Rotas de navegação
const val HOME_ROUTE = "home"
const val EVENTO_DETALHES_ROUTE = "evento_detalhes/{eventoId}"
const val TODOS_EVENTOS_ROUTE = "todos_eventos"

private const val TAG = "HomeScreen"

@Composable
fun HomeScreenNavigation() {
    val navController = rememberNavController()
    val viewModel: EventoViewModel = getViewModel()
    val viewModelUserEvento: EventoUserViewModel = getViewModel()
    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()

    // Coletar estados do ViewModel usando collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val eventosCurtidos = viewModelUserEvento.eventosCurtidos

    // Carregar eventos quando a tela for iniciada
    LaunchedEffect(Unit) {
        Log.d(TAG, "Iniciando carregamento de eventos")
        viewModel.carregarEventos()
    }

    // Carregar favoritos quando o userId estiver disponível
    LaunchedEffect(userId) {
        if (userId != -1) {
            Log.d(TAG, "Carregando favoritos para usuário $userId")
            viewModelUserEvento.carregarEventosCurtidos()
            viewModelUserEvento.carregarEventosConfirmados()
        }
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        Log.d(TAG, "HomeScreen - Recarregando favoritos após navegação")
        viewModelUserEvento.carregarEventosCurtidos()
    }

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(
                navController = navController,
                eventos = viewModel.eventos,
                isLoading = isLoading,
                onFavoritoClick = { evento ->
                    evento.id?.let { eventoId ->
                        viewModelUserEvento.alternarCurtir(userId, eventoId)
                    }
                }
            )
        }

        composable(TODOS_EVENTOS_ROUTE) {
            TodosEventosScreen(
                navController = navController,
                eventos = viewModel.eventos,
                onCurtitClick = { evento ->
                    evento.id?.let { eventoId ->
                        viewModelUserEvento.alternarCurtir(userId, eventoId)
                    }
                },
            )
        }

        // Dentro do NavHost, na parte do composable EVENTO_DETALHES_ROUTE
        composable(
            route = EVENTO_DETALHES_ROUTE,
            arguments = listOf(navArgument("eventoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getInt("eventoId") ?: return@composable

            // Encontrar o evento pelo ID
            val evento = viewModel.eventos.find { it.id == eventoId }
            if (evento != null) {
                // Criar um EventoUser para passar para a tela de detalhes
                val eventoUser = EventoUser(
                    UsuarioId = userId,
                    EventoId = eventoId,
                    confirmado = viewModelUserEvento.isEventoConfirmado(eventoId),
                    curtir = viewModelUserEvento.isEventoFavoritoFlow(eventoId).collectAsState(initial = false).value
                )

                EventoDetalhesScreen(
                    navController = navController,
                    evento = evento,
                    eventoUsuario = eventoUser,
                    onFavoritoClick = { evento ->
                        evento.id?.let { eventoId ->
                            viewModelUserEvento.alternarCurtir(userId, eventoId)
                            // Force refresh of favorites after toggling
                            viewModelUserEvento.carregarEventosCurtidos()
                        }
                    },
                )
            } else {
                // Caso o evento não seja encontrado
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Evento não encontrado")
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    eventos: List<Evento>,
    isLoading: Boolean,
    onFavoritoClick: (Evento) -> Unit,
    userViewModel: UserViewModel = getViewModel()
) {
    Log.d(TAG, "Renderizando HomeScreen com ${eventos.size} eventos")

    // Use o Koin para obter os ViewModels diretamente
    val perfilViewModel: PerfilViewModel = getViewModel()
    val perfilUiState by perfilViewModel.perfilUiState.collectAsState()
    val viewModelUserEvento: EventoUserViewModel = getViewModel()

    // ViewModel para o chat
    val cadastroViewModel: CadastroViewModel = getViewModel()
    val trocaEnderecoViewModel: TrocaEnderecoViewModel = getViewModel()

    // Estados
    var searchText by remember { mutableStateOf("") }
    var filtroSelecionado by remember { mutableStateOf("") }

    val userId by userViewModel.userId.collectAsState()

    // Efeito para carregar o perfil do usuário quando o ID estiver disponível
    LaunchedEffect(key1 = userId) {
        if (userId != -1) {
            Log.d(TAG, "Buscando perfil para usuário ID: $userId")
            perfilViewModel.buscarPerfil(userId)
        } else {
            Log.d(TAG, "ID do usuário não encontrado no UserViewModel/SharedPreferences.")
        }
    }

    // Cores do tema
    val backgroundColor = Color(0xFF1C1D21)
    val primaryColor = Color(0xFF3B5FE9)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val searchBarColor = Color(0xFF2A2B30)
    val buttonInactiveColor = Color(0xFF808080)

    // Filtrar eventos por pesquisa e período
    val eventosFiltrados = remember(filtroSelecionado, searchText, eventos) {
        val filtrados = if (searchText.isEmpty()) {
            eventos
        } else {
            eventos.filter { evento ->
                evento.titulo?.contains(searchText, ignoreCase = true) == true ||
                        evento.descricao?.contains(searchText, ignoreCase = true) == true
            }
        }

        val hoje = java.time.LocalDate.now()
        val inicioDaSemana = hoje.with(java.time.DayOfWeek.MONDAY)
        val fimDaSemana = hoje.with(java.time.DayOfWeek.SUNDAY)
        val inicioDoMes = hoje.withDayOfMonth(1)
        val fimDoMes = hoje.withDayOfMonth(hoje.lengthOfMonth())

        val zona = java.time.ZoneId.systemDefault()

        val eventosEstaSemana = filtrados.filter { evento ->
            evento.data?.toInstant()?.atZone(zona)?.toLocalDate()?.let { dataEvento ->
                dataEvento in inicioDaSemana..fimDaSemana
            } ?: false
        }

        val eventosEsteMes = filtrados.filter { evento ->
            evento.data?.toInstant()?.atZone(zona)?.toLocalDate()?.let { dataEvento ->
                dataEvento in inicioDoMes..fimDoMes
            } ?: false
        }

        when (filtroSelecionado) {
            "Esta semana" -> eventosEstaSemana
            "Este Mês" -> eventosEsteMes
            else -> filtrados
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("tela_home"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 40.dp, bottom = 80.dp)
        ) {
            // Cabeçalho com saudação e foto de perfil
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Olá, ${perfilUiState.usuario?.nome ?: "Visitante"} ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.testTag("titulo_bem_vindo"),
                        )
                        Text(
                            text = "👋",
                            fontSize = 32.sp
                        )
                    }
                    Text(
                        text = "Venha para nossos eventos",
                        fontSize = 18.sp,
                        color = secondaryTextColor,
                        modifier = Modifier.testTag("subtitulo_bem_vindo"),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de pesquisa
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    Log.d(TAG, "Texto de pesquisa alterado para: $it")
                    searchText = it
                },
                placeholder = { Text("Pesquisar eventos", color = secondaryTextColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("barra_pesquisa"),
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = searchBarColor,
                    unfocusedContainerColor = searchBarColor,
                    disabledContainerColor = searchBarColor,
                    focusedBorderColor = searchBarColor,
                    unfocusedBorderColor = searchBarColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = textColor,
                        modifier = Modifier.size(28.dp)
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título "Próximos eventos" e "Ver todos"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximos eventos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Text(
                    text = "Ver todos",
                    fontSize = 16.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.clickable {
                        Log.d(TAG, "Navegando para todos os eventos")
                        navController.navigate(TODOS_EVENTOS_ROUTE)
                    }
                        .testTag("ver_todos_eventos"),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de filtro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        Log.d(TAG, "Filtro alterado para: Esta semana")
                        filtroSelecionado = "Esta semana"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filtroSelecionado == "Esta semana") primaryColor else buttonInactiveColor
                    )
                ) {
                    Text(
                        text = "Esta semana",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        Log.d(TAG, "Filtro alterado para: Este Mês")
                        filtroSelecionado = "Este Mês"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filtroSelecionado == "Este Mês") primaryColor else buttonInactiveColor
                    )
                ) {
                    Text(
                        text = "Este Mês",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador de carregamento
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
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
                            text = "Carregando eventos...",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Mensagem quando não há eventos
            else if (eventosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
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
                            text = if (searchText.isEmpty() && eventos.isNotEmpty())
                                "Nenhum evento encontrado para o período selecionado"
                            else if (searchText.isNotEmpty())
                                "Nenhum evento encontrado para '$searchText'"
                            else
                                "Nenhum evento encontrado",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Carrossel de eventos
            else {
                if (filtroSelecionado == "") {
                    filtroSelecionado = "Esta semana"
                    Log.d(TAG, "Renderizando eventos da semana")
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(eventosFiltrados) { evento ->
                        Log.d(TAG, "Renderizando evento no carrossel: ${evento.id}, ${evento.titulo}")

                        // Passar o estado de curtida diretamente para o EventoCard
                        EventoCard(
                            evento = evento,
                            onFavoritoClick = {
                                Log.d(TAG, "Curtir clicado para evento ${evento.id}")
                                viewModelUserEvento.alternarCurtir(userId, evento.id!!)
                                // Force refresh of favorites after toggling
                                viewModelUserEvento.carregarEventosCurtidos()
                            },
                            onClick = {
                                Log.d(TAG, "Navegando para detalhes do evento ${evento.id}")
                                navController.navigate("evento_detalhes/${evento.id}")
                            }
                        )
                    }
                }
            }
        }

        // Chat component integrado corretamente
        IgrejaChatComponent(
            usuarioLogado = perfilUiState.usuario, // Usar o usuário do perfil
            userId = userId,
            cadastroViewModel = cadastroViewModel,
            modifier = Modifier.fillMaxSize(),
            onAtualizacaoEnderecoEnviada = { endereco ->
                // Lógica para processar a atualização de endereço
                Log.d(TAG, "Atualização de endereço recebida: ${endereco.logradouro}, ${endereco.numero}")
                trocaEnderecoViewModel.atualizarEndereco(userId, endereco)
            },
            onPedidoOracaoEnviado = { pedido ->
                // Lógica para processar pedido de oração
                Log.d(TAG, "Pedido de oração recebido: ${pedido.descricao}")
                // Implementar lógica para salvar o pedido de oração
            }
        )
    }
}

@Preview()
@Composable
fun EventosAppPreview() {
    HomeScreenNavigation()
}
