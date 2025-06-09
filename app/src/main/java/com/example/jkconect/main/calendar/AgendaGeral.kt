package com.example.jkconect.main.calendar

import Evento
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jkconect.ui.theme.AzulPrincipal
import com.example.jkconect.viewmodel.EventoViewModel
import com.example.jkconect.viewmodel.EventoUserViewModel
import com.example.jkconect.data.api.UserViewModel
import org.koin.androidx.compose.getViewModel
import java.util.*

// Cores para o tema escuro
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkCardBackground = Color(0xFF252525)
val DarkText = Color(0xFFE1E1E1)
val DarkTextSecondary = Color(0xFFAAAAAA)
val AzulDestaque = Color(0xFF2196F3)

@Composable
fun CustomCalendar(
    eventos: List<Evento>,
    onDateSelected: (Calendar) -> Unit
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    // Criar mapa de eventos por data
    val eventosPorData = eventos.groupBy { evento ->
        val cal = Calendar.getInstance()
        evento.data?.let { cal.time = it }
        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Cabeçalho do calendário com gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AzulDestaque.copy(alpha = 0.7f),
                                AzulDestaque.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentMonth = Calendar.getInstance().apply {
                                time = currentMonth.time
                                add(Calendar.MONTH, -1)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Mês anterior",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "${getMonthName(currentMonth.get(Calendar.MONTH))} ${currentMonth.get(Calendar.YEAR)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            currentMonth = Calendar.getInstance().apply {
                                time = currentMonth.time
                                add(Calendar.MONTH, 1)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Próximo mês",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dias da semana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkTextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )

            // Grid de dias
            val daysInMonth = getDaysInMonth(currentMonth)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(daysInMonth) { day ->
                    if (day != null) {
                        val dayKey = "${currentMonth.get(Calendar.YEAR)}-${currentMonth.get(Calendar.MONTH)}-$day"
                        val hasEvents = eventosPorData.containsKey(dayKey)
                        val isSelected = selectedDate?.let {
                            it.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                                    it.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                    it.get(Calendar.DAY_OF_MONTH) == day
                        } ?: false
                        val isToday = isToday(currentMonth, day)

                        DayCell(
                            day = day,
                            hasEvents = hasEvents,
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = {
                                val newSelectedDate = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentMonth.get(Calendar.YEAR))
                                    set(Calendar.MONTH, currentMonth.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, day)
                                }
                                selectedDate = newSelectedDate
                                onDateSelected(newSelectedDate)
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    hasEvents: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> AzulDestaque
            isToday -> AzulDestaque.copy(alpha = 0.3f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (hasEvents && !isSelected) 2.dp else 0.dp,
                color = if (hasEvents) AzulDestaque else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                color = when {
                    isSelected -> Color.White
                    isToday -> AzulDestaque
                    else -> DarkText
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // Pequeno indicador para eventos
            if (hasEvents && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(AzulDestaque, CircleShape)
                )
            }
        }
    }
}

@Composable
fun AgendaGeral(navController: NavController) {
    val eventoViewModel: EventoViewModel = getViewModel()
    val eventoUserViewModel: EventoUserViewModel = getViewModel()
    val userViewModel: UserViewModel = getViewModel()

    val eventos = eventoViewModel.eventos
    val userId by userViewModel.userId.collectAsState()

    // Inicializar com o dia de hoje
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Filtrar eventos da data selecionada
    val eventosDoDay = eventos.filter { evento ->
        evento.data?.let { eventoData ->
            val eventoCal = Calendar.getInstance().apply { time = eventoData }
            eventoCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                    eventoCal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                    eventoCal.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
        } ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {

        // Calendário personalizado
        CustomCalendar(
            eventos = eventos,
            onDateSelected = { date ->
                selectedDate = date
            }
        )

        // Lista de eventos do dia selecionado (sempre visível)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCardBackground
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = AzulDestaque,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val today = Calendar.getInstance()
                    val isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                    Text(
                        text = if (isToday) "Eventos de hoje" else "Eventos de ${selectedDate.get(Calendar.DAY_OF_MONTH)}/${selectedDate.get(Calendar.MONTH) + 1}/${selectedDate.get(Calendar.YEAR)}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Badge com número de eventos
                    if (eventosDoDay.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = AzulDestaque,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${eventosDoDay.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (eventosDoDay.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(eventosDoDay) { evento ->
                            EventCard(
                                evento = evento,
                                onClick = {
                                    // Usar a mesma navegação do HomeScreen
                                    navController.navigate("evento_detalhes/${evento.id}")
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = DarkTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val today = Calendar.getInstance()
                            val isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                    selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                    selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                            Text(
                                text = if (isToday) "Nenhum evento para hoje" else "Nenhum evento nesta data",
                                color = DarkTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    evento: Evento,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador colorido com horário
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AzulDestaque,
                                AzulDestaque.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val calendar = Calendar.getInstance()
                evento.data?.let { calendar.time = it }

                Text(
                    text = "",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Detalhes do evento
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo ?: "Evento sem título",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = DarkText
                )
                if (!evento.descricao.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = evento.descricao,
                        fontSize = 14.sp,
                        color = DarkTextSecondary,
                        maxLines = 2
                    )
                }
            }

            // Ícone indicando que é clicável
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Ver detalhes",
                tint = DarkTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Funções auxiliares
fun getDaysInMonth(calendar: Calendar): List<Int?> {
    val cal = Calendar.getInstance().apply {
        time = calendar.time
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val days = mutableListOf<Int?>()

    // Adicionar espaços vazios para os dias antes do primeiro dia do mês
    repeat(firstDayOfWeek) {
        days.add(null)
    }

    // Adicionar os dias do mês
    for (day in 1..daysInMonth) {
        days.add(day)
    }

    return days
}

fun getMonthName(month: Int): String {
    return when (month) {
        0 -> "Janeiro"
        1 -> "Fevereiro"
        2 -> "Março"
        3 -> "Abril"
        4 -> "Maio"
        5 -> "Junho"
        6 -> "Julho"
        7 -> "Agosto"
        8 -> "Setembro"
        9 -> "Outubro"
        10 -> "Novembro"
        11 -> "Dezembro"
        else -> ""
    }
}

fun isToday(calendar: Calendar, day: Int): Boolean {
    val today = Calendar.getInstance()
    return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
            today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
            today.get(Calendar.DAY_OF_MONTH) == day
}