package com.example.jkconect.main.myevents

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jkconect.R
import com.example.jkconect.main.calendar.CardMinhaAgenda
import com.example.jkconect.main.calendar.WhiteDivider
import com.example.jkconect.main.calendar.services.CardAgenda
import com.example.jkconect.ui.theme.AzulPrincipal
import com.example.jkconect.ui.theme.RobotoCondensedFontFamily

@Composable
fun MyEvents() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

      MeusCurtidos()

    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventsPreview() {
    MyEvents()
}

@Composable
fun MeusCurtidos() {

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
            text = "Eventos curtidos",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        WhiteDivider()

        LazyColumn(
            modifier = Modifier
                .height(600.dp)
        ) {

            items(cardsEvento) { card ->
                CardEventos(
                    titulo = card.titulo,
                    confirmados = card.confirmados,
                    data = card.data
                )
            }

        }

    }
}

@Composable
fun CardEventos(
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
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "confirmados - $confirmados",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Text(
                        text = "Data: $data",
                        color = Color.White,
                        fontSize = 12.sp,
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
                                text = "Remover",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }
        }
    }
}
