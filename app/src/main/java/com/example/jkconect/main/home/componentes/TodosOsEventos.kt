package com.example.jkconect.main.home.componentes

import Evento
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jkconect.data.api.UserViewModel
import com.example.jkconect.model.EventoUser
import com.example.jkconect.viewmodel.EventoViewModel
import org.koin.androidx.compose.getViewModel

private const val TAG = "TodosEventosScreen"

@Composable
fun TodosEventosScreen(
    navController: NavController,
    eventos: List<Evento>,
) {
    Log.d(TAG, "Iniciando TodosEventosScreen com ${eventos.size} eventos")

    val userViewModel: UserViewModel = getViewModel()
    val userId by userViewModel.userId.collectAsState()
    val eventoViewModel: EventoViewModel = getViewModel()
    val isLoading by eventoViewModel.isLoading.collectAsState()

    // Estados
    var searchText by remember { mutableStateOf("") }

    // Cores
    val backgroundColor = Color(0xFF121212)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val searchBarColor = Color(0xFF2A2B30)
    val primaryColor = Color(0xFF3B5FE9)

    // Filtrar eventos por pesquisa
    val eventosFiltrados = remember(searchText, eventos) {
        Log.d(TAG, "Filtrando eventos com texto: '$searchText'")
        if (searchText.isEmpty()) {
            eventos
        } else {
            eventos.filter { evento ->
                val matchesTitle = evento.titulo?.contains(searchText, ignoreCase = true) == true
                val matchesDescription = evento.descricao?.contains(searchText, ignoreCase = true) == true
                matchesTitle || matchesDescription
            }
        }
    }

    LaunchedEffect(eventosFiltrados) {
        Log.d(TAG, "Eventos filtrados: ${eventosFiltrados.size}")
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
                    onClick = {
                        Log.d(TAG, "Botão voltar clicado")
                        navController.popBackStack()
                    },
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
                onValueChange = {
                    Log.d(TAG, "Texto de pesquisa alterado para: $it")
                    searchText = it
                },
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
                            text = "Carregando eventos...",
                            fontSize = 18.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Lista de eventos
            else if (eventosFiltrados.isEmpty()) {
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
                            text = if (searchText.isEmpty())
                                "Nenhum evento encontrado"
                            else
                                "Nenhum evento encontrado para '$searchText'",
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
                        Log.d(TAG, "Renderizando evento na lista: ${evento.id}, ${evento.titulo}")

                        // Crie o EventoUser diretamente, sem verificação condicional
                        // Isso evita o NullPointerException
                        val eventoUser = EventoUser(
                            UsuarioId = userId,
                            EventoId = evento.id ?: 0, // Use 0 como fallback se o ID for nulo
                            confirmado = false,
                            curtir = eventoViewModel.isEventoFavorito(evento.id ?: 0)
                        )

                        // Não é necessário verificar se eventoUser é nulo, pois ele nunca será nulo agora
                        EventoCardHorizontal(
                            evento = evento,
                            eventoUsuario = eventoUser,
                            onFavoritoClick = {
                                Log.d(TAG, "Favorito clicado para evento ${evento.id}")
                                evento.id?.let { eventoId ->
//                                    evento.alternarFavorito(eventoId)
                                }
                            },
                            onClick = {
                                Log.d(TAG, "Navegando para detalhes do evento ${evento.id}")
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