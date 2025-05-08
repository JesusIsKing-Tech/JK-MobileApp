package com.example.jkconect.main.home.componentes

import Evento
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.data.api.formatarData
import com.example.jkconect.model.EventoUser
import com.example.jkconect.viewmodel.EventoUserViewModel
import com.example.jkconect.viewmodel.EventoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import RetrofitClient

private const val TAG = "EventoDetalhesScreen"

@Composable
fun EventoDetalhesScreen(
    navController: NavController,
    evento: Evento,
    eventoUsuario: EventoUser,
    onFavoritoClick: () -> Unit,
) {
    Log.d(TAG, "Renderizando detalhes do evento: ${evento.id}, ${evento.titulo}")

    val viewModel: EventoViewModel = getViewModel()
    val eventoUserViewModel: EventoUserViewModel = getViewModel()
    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()

    val eventosConfirmados by eventoUserViewModel.eventosConfirmados.collectAsState()
    val isLoading by eventoUserViewModel.isLoading.collectAsState()
    val errorMessage by eventoUserViewModel.errorMessage.collectAsState()
    val successMessage by eventoUserViewModel.successMessage.collectAsState()

    // Verificar se o evento tem presença confirmada
    val isConfirmado = remember(eventosConfirmados) {
        evento.id?.let { eventosConfirmados.contains(it) } ?: false
    }

    var presencaConfirmada by remember { mutableStateOf(isConfirmado) }
    val scrollState = rememberScrollState()
    var contagemPresencas by remember { mutableStateOf<Long?>(0) }
    val scope = rememberCoroutineScope()

    // Estado para a imagem do evento
    val _eventImageBitmap = remember { MutableStateFlow<ImageBitmap?>(null) }
    val eventImageBitmap by _eventImageBitmap.collectAsState()

    // Estado de carregamento da imagem
    var isImageLoading by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    // Atualizar presencaConfirmada quando eventosConfirmados mudar
    LaunchedEffect(eventosConfirmados) {
        presencaConfirmada = evento.id?.let { eventosConfirmados.contains(it) } ?: false
    }

    // Carregar imagem do evento
    LaunchedEffect(evento.imagem) {
        if (!evento.imagem.isNullOrEmpty()) {
            isImageLoading = true
            imageError = null

            scope.launch {
                try {
                    val imageUrl = when {
                        evento.imagem.startsWith("http") -> evento.imagem
                        evento.imagem.startsWith("/") -> {
                            try {
                                "${RetrofitClient.BASE_URL}${evento.imagem.removePrefix("/")}"
                            } catch (e: Exception) {
                                Log.e(TAG, "Erro ao construir URL da imagem: ${e.message}")
                                "https://via.placeholder.com/300x200?text=Evento"
                            }
                        }
                        evento.imagem.startsWith("data:") || evento.imagem.length > 100 -> evento.imagem
                        else -> "https://via.placeholder.com/300x200?text=Evento"
                    }

                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()

                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap != null) {
                        _eventImageBitmap.value = bitmap.asImageBitmap()
                    } else {
                        imageError = "Não foi possível carregar a imagem"
                    }

                    inputStream.close()
                    connection.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao carregar imagem: ${e.message}", e)
                    imageError = e.message
                } finally {
                    isImageLoading = false
                }
            }
        }
    }

    LaunchedEffect(evento.id) {
        Log.d(TAG, "LaunchedEffect: Carregando contagem de presenças para evento ${evento.id}")
        evento.id?.let {
            try {
                val contagem = viewModel.contarConfirmacoesPresenca(it)
                Log.d(TAG, "Contagem de presenças recebida: $contagem")
                contagemPresencas = contagem
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar contagem de presenças", e)
            }
        }
    }

    // Mostrar SnackBar para mensagens de erro ou sucesso
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage, successMessage) {
        when {
            errorMessage != null -> {
                snackbarHostState.showSnackbar(
                    message = errorMessage ?: "Erro desconhecido",
                    duration = SnackbarDuration.Short
                )
                eventoUserViewModel.limparErro()
            }
            successMessage != null -> {
                snackbarHostState.showSnackbar(
                    message = successMessage ?: "Operação realizada com sucesso",
                    duration = SnackbarDuration.Short
                )
                eventoUserViewModel.limparSucesso()
            }
        }
    }

    // Cores
    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF3B5FE9)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val confirmedColor = Color(0xFF2E7D32)
    val cancelColor = Color(0xFFB71C1C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Conteúdo rolável
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Card do evento
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                ) {
                    // Área da imagem
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isImageLoading -> {
                                // Mostrar indicador de carregamento
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF3B5FE9),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            imageError != null -> {
                                // Mostrar erro
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Erro ao carregar imagem",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            eventImageBitmap != null -> {
                                // Mostrar imagem carregada
                                Image(
                                    bitmap = eventImageBitmap!!,
                                    contentDescription = evento.titulo ?: "Evento",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                // Placeholder quando não há imagem
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = evento.titulo ?: "Evento",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Botão de voltar
                    IconButton(
                        onClick = {
                            Log.d(TAG, "Botão voltar clicado")
                            navController.popBackStack()
                        },
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
                        onClick = {
                            Log.d(TAG, "Botão favorito clicado para evento ${evento.id}")
                            onFavoritoClick()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (eventoUsuario.curtir == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (eventoUsuario.curtir == true) Color.Red else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Informações do local + título + valor
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF808080))
                            .padding(16.dp),
                    ) {
                        // Título do evento
                        evento.titulo?.let {
                            Text(
                                text = it,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Coluna com local e endereço
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Local",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = evento.endereco ?: "Endereço não disponível",
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Valor
                            evento.valor?.let { valor ->
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text(text = "Valor", fontSize = 14.sp, color = Color.White)
                                    Text(
                                        text = if (valor > 0) {
                                            NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
                                        } else {
                                            "Gratuito"
                                        },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
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
                    InformacaoItem(
                        icon = Icons.Default.AccessTime,
                        info = evento.horario ?: "10:00"
                    )

                    evento.data?.let { formatarData(it) }?.let {
                        InformacaoItem(
                            icon = Icons.Default.DateRange,
                            info = it
                        )
                    } ?: InformacaoItem(
                        icon = Icons.Default.DateRange,
                        info = "Data não disponível"
                    )

                    InformacaoItem(
                        icon = Icons.Default.People,
                        info = contagemPresencas?.toString() ?: "0",
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Descrição
                evento.descricao?.let {
                    Text(
                        text = "Descrição",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = it,
                        fontSize = 16.sp,
                        color = secondaryTextColor,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Botão de confirmar presença ou mensagem de presença confirmada
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 30.dp, vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (presencaConfirmada) confirmedColor else primaryColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    // Mostrar indicador de carregamento
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else if (presencaConfirmada) {
                    // Mostrar opção para cancelar presença
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            Log.d(TAG, "Cancelando presença para evento ${evento.id}")
                            evento.id?.let {
                                eventoUserViewModel.cancelarPresenca(userId, it)
                                scope.launch {
                                    // Atualizar UI após um breve delay para dar tempo da API processar
                                    delay(500)
                                    viewModel.contarConfirmacoesPresenca(it)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Presença confirmada ✓ (Clique para cancelar)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                        )
                    }
                } else {
                    // Mostrar opção para confirmar presença
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            Log.d(TAG, "Confirmando presença para evento ${evento.id}")
                            evento.id?.let {
                                eventoUserViewModel.confirmarPresenca(userId, it)
                                scope.launch {
                                    // Atualizar UI após um breve delay para dar tempo da API processar
                                    delay(500)
                                    viewModel.contarConfirmacoesPresenca(it)
                                }
                            }
                        }
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

        // SnackBar para mensagens
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }
}

@Composable
fun InformacaoItem(icon: ImageVector, info: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF3B5FE9), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = info,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}