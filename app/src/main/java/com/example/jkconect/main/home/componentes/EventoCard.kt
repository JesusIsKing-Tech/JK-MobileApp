package com.example.jkconect.main.home.componentes

import Evento
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.viewmodel.EventoViewModel
import com.example.jkconect.viewmodel.EventoUserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel

private const val TAG = "EventoCard"

@Composable
fun EventoCard(
    evento: Evento,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    val viewModel: EventoViewModel = getViewModel()
    val eventoUserViewModel: EventoUserViewModel = getViewModel()
    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()

    // Verificar se o evento está curtido usando o StateFlow
    val isCurtido by eventoUserViewModel.isEventoFavoritoFlow(evento.id).collectAsState(initial = false)

    // Verificar se o evento tem presença confirmada
    val isPresencaConfirmada by remember(eventoUserViewModel.eventosConfirmados) {
        derivedStateOf {
            evento.id?.let { eventoUserViewModel.isEventoConfirmado(it) } ?: false
        }
    }

    // Estado local para controlar o estado de presença
    var localIsPresencaConfirmada by remember { mutableStateOf(false) }

    // Sincronizar o estado local com o estado do ViewModel
    LaunchedEffect(isPresencaConfirmada) {
        localIsPresencaConfirmada = isPresencaConfirmada
    }

    LaunchedEffect(evento.id) {
        Log.d(TAG, "EventoCard observando estado de curtida para evento ${evento.id}")
        eventoUserViewModel.carregarEventosCurtidos()
        eventoUserViewModel.carregarEventosConfirmados()
    }

    var contagemPresencas by remember { mutableStateOf(0L) }

    // Estado de carregamento da imagem
    var isImageLoading by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Carregar contagem de presenças
    LaunchedEffect(evento.id) {
        evento.id?.let {
            try {
                val contagem = viewModel.contarConfirmacoesPresenca(it)
                contagemPresencas = contagem
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao contar presenças", e)
            }
        }
    }

    // Usar rememberCoroutineScope para obter um escopo que sobreviva às recomposições
    val coroutineScope = rememberCoroutineScope()
    val _eventImageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    // Efeito para carregar a imagem
    LaunchedEffect(evento.id) {
        if (evento.id != null) {
            isImageLoading = true
            imageError = null

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val responseBody = viewModel.imagemEvento(evento.id)
                    val bytes = responseBody.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            _eventImageBitmap.value = bitmap.asImageBitmap()
                        } else {
                            imageError = "Não foi possível decodificar a imagem"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("EventoCard", "Erro ao carregar imagem: ${e.message}", e)
                        imageError = e.message
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        isImageLoading = false
                    }
                }
            }
        }
    }

    // Formatar a data
    val dataFormatada = evento.data?.let { data ->
        SimpleDateFormat("dd MMM", Locale("pt", "BR")).format(data)
    } ?: "Data não disponível"

    Card(
        modifier = Modifier
            .width(300.dp)
            .height(400.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Área da imagem
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when {
                    isImageLoading -> {
                        // Mostrar indicador de carregamento
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
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
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Erro ao carregar imagem",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    _eventImageBitmap.value != null -> {
                        // Mostrar imagem carregada
                        Image(
                            bitmap = _eventImageBitmap.value!!,
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
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = evento.titulo ?: "Evento",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Botão de favorito com animação
            var animateScale by remember { mutableStateOf(1f) }

            LaunchedEffect(isCurtido) {
                if (isCurtido) {
                    animateScale = 1.2f
                    kotlinx.coroutines.delay(150)
                    animateScale = 1f
                }
            }

            // Row para os botões no topo
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão de favorito
                IconButton(
                    onClick = {
                        Log.d(TAG, "Botão de favorito clicado para evento ${evento.id}")
                        onFavoritoClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isCurtido) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isCurtido) "Descurtir" else "Curtir",
                        tint = if (isCurtido) Color.Red else Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(animateScale)
                    )
                }
            }

            // Informações do evento na parte inferior
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF808080))
                    .padding(10.dp)
            ) {
                // Título do evento
                evento.titulo?.let {
                    Text(
                        text = it,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Data e horário do evento
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dataFormatada,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Valor do evento, se houver
                evento.valor?.let { valor ->
                    if (valor > 0) {
                        Text(
                            text = "R$ ${"%.2f".format(valor)}",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = evento.endereco ?: "erro ao puxar endereco",
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = contagemPresencas.toString(),
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}