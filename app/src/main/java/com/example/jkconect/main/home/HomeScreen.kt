import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Modelo de dados para eventos
data class Evento(
    val id: String,
    val titulo: String,
    val imagem: String,
    val data: String,
    val dataCompleta: String,
    val horario: String,
    val local: String,
    val endereco: String,
    val valor: String,
    val descricao: String,
    val participantes: Int,
    var favorito: Boolean = false,
    val detalhes: String,
    val dataEvento: Date // Data para filtrar eventos
)

// Rotas de navegação
const val HOME_ROUTE = "home"
const val EVENTO_DETALHES_ROUTE = "evento_detalhes/{eventoId}"
const val TODOS_EVENTOS_ROUTE = "todos_eventos"

@Composable
fun HomeScreen() {
    val navController = rememberNavController()

    // Formato para parsing de datas
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Lista de eventos de exemplo
    val eventosState = remember {
        mutableStateListOf(
            Evento(
                id = "1",
                titulo = "RETIRO ENTRE MULHERES",
                imagem = "https://pbs.twimg.com/media/FrGg3HpWcAAvMbo.jpg:large",
                data = "14-16",
                dataCompleta = "DE 14 A 16 DE MARÇO DE 2025",
                horario = "20:00",
                local = "Chácara PIBVM",
                endereco = "Arujá, São Paulo",
                valor = "R$ 200",
                descricao = "This vast mountain range is renowned for its remarkable diversity in terms of topography and climate. It features towering peaks, active volcanoes, deep canyons, expansive plateaus, and lush valleys.",
                participantes = 48,
                favorito = false,
                detalhes = "Investimento R$ 200,00 (sinal de R$ 50,00 no ato da inscrição + parcelamento até 09/03/2025)\n\nPara participantes a partir dos 18 anos",
                dataEvento = dateFormat.parse("14/03/2025")!!
            ),
            Evento(
                id = "2",
                titulo = "ACAMPAMENTO JOVEM",
                imagem = "https://i.pinimg.com/736x/7d/c4/fc/7dc4fccd6ee84390d01b3fea3c34c80a.jpg",
                data = "5-7",
                dataCompleta = "DE 5 A 7 DE ABRIL DE 2025",
                horario = "18:00",
                local = "Sítio Esperança",
                endereco = "Souto, São Paulo",
                valor = "R$ 180",
                descricao = "Uma experiência incrível para jovens se conectarem com a natureza e desenvolverem sua espiritualidade em um ambiente acolhedor e divertido.",
                participantes = 65,
                favorito = false,
                detalhes = "Investimento R$ 180,00 (sinal de R$ 50,00 no ato da inscrição + parcelamento até 25/03/2025)",
                dataEvento = dateFormat.parse("05/04/2025")!!
            ),
            Evento(
                id = "3",
                titulo = "CULTO DE ADORAÇÃO",
                imagem = "https://img.freepik.com/psd-premium/modelo-de-banner-da-web-de-conferencia-de-culto_160623-238.jpg",
                data = "28",
                dataCompleta = "28 DE ABRIL DE 2025",
                horario = "19:30",
                local = "Igreja PIBVM",
                endereco = "Centro, São Paulo",
                valor = "Gratuito",
                descricao = "Um momento especial de adoração e comunhão com a presença de Deus. Venha participar deste culto especial com toda a família.",
                participantes = 120,
                favorito = false,
                detalhes = "Entrada gratuita\n\nAberto para todas as idades",
                dataEvento = dateFormat.parse("28/04/2025")!!
            ),
            Evento(
                id = "4",
                titulo = "CONFERÊNCIA DE MISSÕES",
                imagem = "https://marketplace.canva.com/EAGVwrOkpBA/2/0/900w/canva-encontro-jovem-culto-igreja-crist%C3%A3-moderno-laranja-preto-e-branco-story-do-instagram-6LnDfByekr4.jpg",
                data = "10-12",
                dataCompleta = "DE 10 A 12 DE MAIO DE 2025",
                horario = "19:00",
                local = "Centro de Convenções",
                endereco = "Campinas, São Paulo",
                valor = "R$ 150",
                descricao = "Uma conferência para despertar o chamado missionário e compartilhar experiências de campo com missionários de diversas partes do mundo.",
                participantes = 200,
                favorito = false,
                detalhes = "Investimento R$ 150,00 (inscrições até 30/04/2025)",
                dataEvento = dateFormat.parse("10/05/2025")!!
            )
        )
    }

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(
                navController = navController,
                eventos = eventosState,
                onFavoritoClick = { evento ->
                    val index = eventosState.indexOfFirst { it.id == evento.id }
                    if (index != -1) {
                        eventosState[index] = evento.copy(favorito = !evento.favorito)
                    }
                }
            )
        }
        composable(
            route = EVENTO_DETALHES_ROUTE
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId")
            val evento = eventosState.find { it.id == eventoId }

            if (evento != null) {
                EventoDetalhesScreen(
                    navController = navController,
                    evento = evento,
                    onFavoritoClick = {
                        val index = eventosState.indexOfFirst { it.id == evento.id }
                        if (index != -1) {
                            eventosState[index] = evento.copy(favorito = !evento.favorito)
                        }
                    }
                )
            }
        }
        composable(TODOS_EVENTOS_ROUTE) {
            TodosEventosScreen(
                navController = navController,
                eventos = eventosState,
                onFavoritoClick = { evento ->
                    val index = eventosState.indexOfFirst { it.id == evento.id }
                    if (index != -1) {
                        eventosState[index] = evento.copy(favorito = !evento.favorito)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    eventos: List<Evento>,
    onFavoritoClick: (Evento) -> Unit
) {
    // Estados
    var searchText by remember { mutableStateOf("") }
    var filtroSelecionado by remember { mutableStateOf("Esta semana") }

    // Cores
    val backgroundColor = Color(0xFF1C1D21)
    val primaryColor = Color(0xFF3B5FE9)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val searchBarColor = Color(0xFF2A2B30)
    val buttonInactiveColor = Color(0xFF808080)

    // Filtrar eventos
    val eventosFiltrados = remember(filtroSelecionado, searchText, eventos) {
        val hoje = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.time = hoje

        // Definir fim da semana atual
        val fimDaSemana = Calendar.getInstance()
        fimDaSemana.time = hoje
        fimDaSemana.add(Calendar.DAY_OF_YEAR, 7)

        // Definir fim do mês atual
        val fimDoMes = Calendar.getInstance()
        fimDoMes.time = hoje
        fimDoMes.add(Calendar.MONTH, 1)

        eventos.filter { evento ->
            // Filtro de texto
            val matchesSearch = if (searchText.isNotEmpty()) {
                evento.titulo.contains(searchText, ignoreCase = true) ||
                        evento.local.contains(searchText, ignoreCase = true) ||
                        evento.endereco.contains(searchText, ignoreCase = true)
            } else {
                true
            }

            // Filtro de período
            val matchesPeriodo = when (filtroSelecionado) {
                "Esta semana" -> evento.dataEvento.before(fimDaSemana.time) && evento.dataEvento.after(hoje)
                "Este Mês" -> evento.dataEvento.before(fimDoMes.time) && evento.dataEvento.after(hoje)
                else -> true
            }

            matchesSearch && matchesPeriodo
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor) // Aplica o fundo primeiro
                .padding(horizontal = 16.dp)
                .background(backgroundColor) // Aplica o fundo primeiro
                .padding(top = 40.dp)
        ) {
            // Cabeçalho com saudação e foto de perfil
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor) // Aplica o fundo primeiro
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Olá, Victor ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "👋",
                            fontSize = 32.sp
                        )
                    }
                    Text(
                        text = "Venha para nossos eventos",
                        fontSize = 18.sp,
                        color = secondaryTextColor
                    )
                }

                // Foto de perfil
                Image(
                    painter = rememberAsyncImagePainter("https://i1.sndcdn.com/artworks-FirCjOYRDNzI3VyP-DXvnCQ-t1080x1080.jpg"),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de pesquisa
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Pesquisar eventos", color = secondaryTextColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
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
                        navController.navigate(TODOS_EVENTOS_ROUTE)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de filtro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { filtroSelecionado = "Esta semana" },
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
                    onClick = { filtroSelecionado = "Este Mês" },
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

            if (eventosFiltrados.isEmpty()) {
                // Mensagem quando não há eventos
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
                            text = "Nenhum evento encontrado",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Carrossel de eventos
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(eventosFiltrados) { evento ->
                        EventoCard(
                            evento = evento,
                            onFavoritoClick = { onFavoritoClick(evento) },
                            onClick = {
                                navController.navigate("evento_detalhes/${evento.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventoCard(
    evento: Evento,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(500.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagem de fundo do evento
            Image(
                painter = rememberAsyncImagePainter(evento.imagem),
                contentDescription = evento.titulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Botão de favorito
            IconButton(
                onClick = onFavoritoClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (evento.favorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favoritar",
                    tint = if (evento.favorito) Color.Red else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Informações do local na parte inferior
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF808080))
                    .padding(16.dp)
            ) {
                // Título do evento (ADICIONADO)
                Text(
                    text = evento.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Local do evento
                Text(
                    text = evento.local,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Endereço e participantes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Localização
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = evento.endereco,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    // Participantes
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = evento.participantes.toString(),
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventoDetalhesScreen(
    navController: NavController,
    evento: Evento,
    onFavoritoClick: () -> Unit
) {
    var presencaConfirmada by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Cores
    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF3B5FE9)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val confirmedColor = Color(0xFF2E7D32) // Verde para presença confirmada

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Card do evento
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                ) {
                    // Imagem de fundo
                    Image(
                        painter = rememberAsyncImagePainter(evento.imagem),
                        contentDescription = evento.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Botão de voltar
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Botão de favorito
                    IconButton(
                        onClick = onFavoritoClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (evento.favorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (evento.favorito) Color.Red else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }



                    // Informações do local
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF808080))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = evento.local,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = evento.endereco,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Valor",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "$${evento.valor.replace("R$ ", "")}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Visão geral
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Visão geral",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Informações do evento
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Horário
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(primaryColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = evento.horario,
                            fontSize = 16.sp,
                            color = textColor
                        )
                    }

                    // Data
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(primaryColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(evento.dataEvento),
                            fontSize = 16.sp,
                            color = textColor
                        )
                    }

                    // Participantes
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(primaryColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = evento.participantes.toString(),
                            fontSize = 16.sp,
                            color = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Descrição
                Text(
                    text = evento.descricao,
                    fontSize = 16.sp,
                    color = secondaryTextColor,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Botão de confirmar presença ou mensagem de presença confirmada
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 30.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 26.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (presencaConfirmada) confirmedColor else primaryColor
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (presencaConfirmada) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Presença confirmada ✓",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { presencaConfirmada = true }
                    ) {
                        Text(
                            text = "Confirmar Presença",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodosEventosScreen(
    navController: NavController,
    eventos: List<Evento>,
    onFavoritoClick: (Evento) -> Unit
) {
    // Estados
    var searchText by remember { mutableStateOf("") }

    // Cores
    val backgroundColor = Color(0xFF121212)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val searchBarColor = Color(0xFF2A2B30)

    // Filtrar eventos por pesquisa
    val eventosFiltrados = remember(searchText, eventos) {
        if (searchText.isEmpty()) {
            eventos
        } else {
            eventos.filter { evento ->
                evento.titulo.contains(searchText, ignoreCase = true) ||
                        evento.local.contains(searchText, ignoreCase = true) ||
                        evento.endereco.contains(searchText, ignoreCase = true)
            }
        }
    }

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
            // Cabeçalho com botão de voltar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2A2B30), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Todos os eventos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Barra de pesquisa
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Pesquisar eventos", color = secondaryTextColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de eventos
            if (eventosFiltrados.isEmpty()) {
                // Mensagem quando não há eventos
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
                            text = "Nenhum evento encontrado",
                            fontSize = 18.sp,
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
                    items(eventosFiltrados) { evento ->
                        EventoCardHorizontal(
                            evento = evento,
                            onFavoritoClick = { onFavoritoClick(evento) },
                            onClick = {
                                navController.navigate("evento_detalhes/${evento.id}")
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
    }
}

@Composable
fun EventoCardHorizontal(
    evento: Evento,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2B30)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagem do evento
            Image(
                painter = rememberAsyncImagePainter(evento.imagem),
                contentDescription = evento.titulo,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            // Informações do evento
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Título do evento
                    Text(
                        text = evento.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    // Botão de favorito
                    IconButton(
                        onClick = onFavoritoClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (evento.favorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (evento.favorito) Color.Red else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Local e data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Local
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = evento.local,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    // Data
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = SimpleDateFormat("dd/MM", Locale.getDefault()).format(evento.dataEvento),
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Preview()
@Composable
fun EventosAppPreview() {
    HomeScreen()
}
