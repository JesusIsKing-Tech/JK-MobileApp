package com.example.jkconect.main.home.componentes

import Evento
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import RetrofitClient
import com.example.jkconect.model.EventoUser
import com.example.jkconect.viewmodel.EventoViewModel
import com.example.jkconect.viewmodel.EventoUserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "EventoCard"

@Composable
fun EventoCard(
    evento: Evento,
    eventoUsuario: EventoUser,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    val viewModel: EventoViewModel = getViewModel()
    val eventoUserViewModel: EventoUserViewModel = getViewModel()
    val eventosCurtidos by eventoUserViewModel.eventosCurtidos.collectAsState()
    val scope = rememberCoroutineScope()

    // Verificar se o evento está curtido
    val isCurtido = remember(eventosCurtidos) {
        evento.id?.let { eventosCurtidos.contains(it) } ?: false
    }

    var contagemPresencas by remember { mutableStateOf(0L) }

    // Estado para a imagem do evento
    val _eventImageBitmap = remember { MutableStateFlow<ImageBitmap?>(null) }
    val eventImageBitmap by _eventImageBitmap.collectAsState()

    // Estado de carregamento da imagem
    var isImageLoading by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

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

    // Função para carregar a imagem do evento
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
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
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

            // Botão de favorito
            IconButton(
                onClick = {
                    Log.d(TAG, "Botão de favorito clicado para evento ${evento.id}")
                    onFavoritoClick()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isCurtido) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isCurtido) "Descurtir" else "Curtir",
                    tint = if (isCurtido) Color.Red else Color.White,
                    modifier = Modifier.size(24.dp)
                )
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
                            text = evento.endereco ?: "rua das flores",
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
