package com.example.jkconect.main.home.componentes

import Evento
import android.content.ContentValues
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jkconect.data.api.formatarData
import android.util.Log
import android.graphics.BitmapFactory
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.jkconect.model.EventoUser
import com.example.jkconect.viewmodel.EventoUserViewModel
import com.example.jkconect.viewmodel.EventoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel

@Composable
fun EventoCardHorizontal(
    evento: Evento,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    val viewModelUserEvento: EventoUserViewModel = getViewModel()
    val viewModel: EventoViewModel = getViewModel()

// Observe o estado de curtida diretamente do ViewModel
    val isCurtido by viewModelUserEvento.isEventoFavoritoFlow(evento.id).collectAsState(initial = false)

    // Estado de carregamento da imagem
    var isImageLoading by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }
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

    // Efeito para recarregar quando houver mudanças nos eventos confirmados
    LaunchedEffect(viewModelUserEvento.isEventoFavoritoFlow(evento.id)) {
        Log.d(ContentValues.TAG, "Lista de eventos confirmados atualizada: ${viewModelUserEvento.eventosConfirmadosCompletos.value.size} eventos")
        // Não precisamos recarregar do backend aqui, pois já estamos observando a lista atualizada
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2B30)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Área da imagem com tratamento de estados
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isImageLoading -> {
                        // Mostrar indicador de carregamento
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    imageError != null -> {
                        // Mostrar erro
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF3A3A3A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Erro",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
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
                                .background(Color(0xFF3A3A3A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = evento.titulo?.take(1)?.uppercase() ?: "E",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

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
                    evento.titulo?.let {
                        Text(
                            text = it,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Botão de favorito
                    IconButton(
                        onClick = onFavoritoClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurtido) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isCurtido) "Descurtir" else "Curtir",
                            tint = if (isCurtido) Color.Red else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Descrição curta, se houver
                evento.descricao?.let { descricao ->
                    if (descricao.isNotEmpty()) {
                        Text(
                            text = descricao,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                            text = evento.endereco ?: "Endereço não disponível",
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                            text = evento.data?.let { formatarData(it) } ?: "Data não disponível",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
