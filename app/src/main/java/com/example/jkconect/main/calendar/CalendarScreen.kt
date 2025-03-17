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
import com.example.jkconect.main.calendar.services.Evento
import com.example.jkconect.ui.theme.AzulPrincipal
import com.example.jkconect.ui.theme.RobotoCondensedFontFamily
import com.example.jkconect.ui.theme.RobotoFontFamily
import java.util.Locale

@Composable
fun CalendarScreen() {

    var selectedScreen by remember { mutableStateOf("Agenda Geral") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
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
                "Minha Agenda" -> MinhaAgenda()
            }
        }
    }

//   CustomCalendar()
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
        Evento(
            12,
            "Março 2025",
            "Qua",
            2
        ),
        Evento(
            13,
            "Março 2025",
            "Qui",
            1
        ),
        Evento(
            14,
            "Março 2025",
            "Qui",
            1
        ),
        Evento(
            15,
            "Março 2025",
            "Qui",
            1
        ),
        Evento(
            16,
            "Março 2025",
            "Qui",
            1
        ),
        Evento(
            17,
            "Março 2025",
            "Qui",
            1
        ),
        Evento(
            18,
            "Março 2025",
            "Qui",
            1
        ),
        Evento(
            19,
            "Março 2025",
            "Qui",
            1
        ),
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
fun MinhaAgenda() {

    val cardsEvento = listOf(
        CardAgenda(
            titulo = "Evento 1",
            confirmados = 10,
            data = "12/03/2025"
        ),
        CardAgenda(
            titulo = "Evento 2",
            confirmados = 10,
            data = "12/03/2025"
        ),
        CardAgenda(
            titulo = "Evento 2",
            confirmados = 10,
            data = "12/03/2025"
        ),
        CardAgenda(
            titulo = "Evento 2",
            confirmados = 10,
            data = "12/03/2025"
        ),
        CardAgenda(
            titulo = "Evento 2",
            confirmados = 10,
            data = "12/03/2025"
        ),
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

        Text(
            text = "Presenças confirmadas",
            color = Color.White,
            fontSize = 36.sp,
            fontFamily = RobotoCondensedFontFamily,
            fontWeight = FontWeight.Bold
        )

        WhiteDivider()

        LazyColumn(
            modifier = Modifier
                .height(600.dp)
        ) {

            items(cardsEvento) { card ->
                CardMinhaAgenda(
                    titulo = card.titulo,
                    confirmados = card.confirmados,
                    data = card.data
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


@Composable
fun CardMinhaAgenda(
    titulo: String,
    confirmados: Int,
    data: String
) {
    Box(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(125.dp)
                    .height(125.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.evento_post),
                    contentDescription = "Evento",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )
            }

            Box() {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = titulo,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = RobotoCondensedFontFamily,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "confirmados - $confirmados",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = RobotoCondensedFontFamily,
                        fontWeight = FontWeight.Normal
                    )

                    Text(
                        text = "Data: $data",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = RobotoCondensedFontFamily,
                        fontWeight = FontWeight.Light
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 14.dp)
                    ) {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzulPrincipal, // Cor de fundo do botão
                                contentColor = Color.White  // Cor do
                            ),
                            modifier = Modifier
                                .size(width = 60.dp, height = 20.dp)
                                .align(Alignment.CenterVertically)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Ver detalhes",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontFamily = RobotoCondensedFontFamily,
                                fontWeight = FontWeight.Bold,

                                )
                        }

                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red, // Cor de fundo do botão
                                contentColor = Color.White  // Cor do
                            ),
                            modifier = Modifier
                                .size(width = 60.dp, height = 20.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 8.sp,
                                fontFamily = RobotoCondensedFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }
        }
    }
}
