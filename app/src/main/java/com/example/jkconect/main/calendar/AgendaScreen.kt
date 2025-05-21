package com.example.jkconect.main.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jkconect.R
import com.example.jkconect.main.calendar.services.CardAgenda
import com.example.jkconect.main.calendar.services.Evento0
import com.example.jkconect.main.home.componentes.EventoCardHorizontal
import com.example.jkconect.main.myevents.MyEventsNavigation
import com.example.jkconect.ui.theme.AzulPrincipal
import com.example.jkconect.ui.theme.RobotoCondensedFontFamily
import com.example.jkconect.ui.theme.RobotoFontFamily
import com.example.jkconect.viewmodel.EventoUserViewModel
import org.koin.androidx.compose.getViewModel
import java.util.Locale

@Composable
fun CalendarScreen() {

    val eventoUserViewModel: EventoUserViewModel = getViewModel()

    var selectedScreen by remember { mutableStateOf("Agenda Geral") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1D21))  // Define a cor de fundo aqui

    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { selectedScreen = "Agenda Geral" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedScreen == "Agenda Geral") Color(0xFF0E48AF) else Color(
                        0xFF888888
                    )
                ),
                modifier = Modifier
                    .size(width = 170.dp, height = 50.dp)
            ) {
                Text("Agenda Geral")
            }

            Button(
                onClick = { selectedScreen = "Minha Agenda" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedScreen == "Minha Agenda") Color(0xFF0E48AF) else Color(
                        0xFF888888
                    )
                ),
                modifier = Modifier
                    .size(width = 170.dp, height = 50.dp)
            ) {
                Text("Minha Agenda")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp)
        ) {
            when (selectedScreen) {
                "Agenda Geral" -> AgendaGeral()
                "Minha Agenda" -> {
                    MinhaAgendaScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    CalendarScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCalendar() {
    val datePickerState = DatePickerState(
        locale = Locale.getDefault(),
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AzulPrincipal,
            secondary = Color.White,
            tertiary = Color.White,
            onBackground = Color.Black,
            background = Color.Black
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(16.dp)
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(24.dp)
                    ),
                title = null, // Remove o título
                headline = null, // Remove o cabeçalho
                showModeToggle = false, // Remove o botão de alternar entre os modos
            )
        }
    }
}

@Composable
fun AgendaGeral() {

    val events = listOf(
        Evento0(
            12,
            "Março 2025",
            "Qua",
            2
        ),
        Evento0(
            13,
            "Março 2025",
            "Qui",
            1
        ),
        Evento0(
            14,
            "Março 2025",
            "Qui",
            1
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomCalendar()

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(250.dp) // Defina um tamanho para ativar a rolagem
        ) {
            items(events) { event ->
                EventDayCard(
                    day = event.day,
                    monthYear = event.monthYear,
                    dayOfWeek = event.dayOfWeek,
                    eventCount = event.eventCount
                )
            }
        }
    }
}

@Composable
fun EventDayCard(
    day: Int,
    monthYear: String,
    dayOfWeek: String,
    eventCount: Int
) {
    Box(
        modifier = Modifier
            .size(width = 350.dp, height = 50.dp)
            .background(color = Color.LightGray, shape = RoundedCornerShape(32.dp))
            .padding(start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$day",
                color = AzulPrincipal,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = monthYear,
                    color = AzulPrincipal,
                    fontSize = 16.sp
                )
                Text(
                    text = dayOfWeek,
                    color = AzulPrincipal,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color = AzulPrincipal, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$eventCount",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}


@Composable
fun WhiteDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(8.dp),
        thickness = 1.dp,
        color = Color.White
    )
}



