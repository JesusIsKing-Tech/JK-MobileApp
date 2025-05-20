// Correção para EventoCardHorizontal.kt
package com.example.jkconect.main.home.componentes

import Evento
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.jkconect.data.api.formatarData
import android.util.Log
import RetrofitClient
import com.example.jkconect.model.EventoUser
import com.example.jkconect.viewmodel.EventoUserViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun EventoCardHorizontal(
    evento: Evento,
    eventoUsuario: EventoUser,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    val viewModelUserEvento: EventoUserViewModel = getViewModel()

    // Função para obter a URL da imagem fora do Composable
    val imageUrl = remember(evento.imagem) {
        if (!evento.imagem.isNullOrEmpty()) {
            when {
                evento.imagem.startsWith("http") -> evento.imagem
                evento.imagem.startsWith("/") -> {
                    try {
                        "${RetrofitClient.BASE_URL}${evento.imagem.removePrefix("/")}"
                    } catch (e: Exception) {
                        Log.e("EventoCardHorizontal", "Erro ao construir URL da imagem: ${e.message}")
                        "https://via.placeholder.com/300x200?text=Evento"
                    }
                }
                evento.imagem.startsWith("data:") || evento.imagem.length > 100 -> evento.imagem
                else -> "https://via.placeholder.com/300x200?text=Evento"
            }
        } else {
            "https://via.placeholder.com/300x200?text=Evento"
        }
    }

    // Agora use o imageUrl para o rememberAsyncImagePainter
    val imagemPainter = rememberAsyncImagePainter(imageUrl)

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
            Image(
                painter = imagemPainter,
                contentDescription = evento.titulo ?: "Evento",
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
                            imageVector = if (evento.id?.let {
                                    viewModelUserEvento.isEventoCurtido(
                                        it
                                    )
                                } ==false) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (eventoUsuario.curtir == true) Color.Red else Color.White,
                            modifier = Modifier.size(20.dp)
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