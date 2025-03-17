package com.example.jkconect.main.presence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jkconect.R
import androidx.compose.ui.unit.sp
import com.example.jkconect.ui.theme.AzulBotao
import com.example.jkconect.ui.theme.CinzaEscuroFundo
import com.example.jkconect.ui.theme.PurpleGrey40

@Composable
fun PresenceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinzaEscuroFundo)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.photo_eventpresence),
                    contentDescription = "Event Image",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Chácara PIBVM",
                        fontSize = 16.sp,
                        color = PurpleGrey40,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Valor",
                        fontSize = 14.sp,
                        color = PurpleGrey40
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_local),
                        contentDescription = "Location Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Arujá, São Paulo",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "R$ 200",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleGrey40 
                    )
                }
            }
        }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Visão geral",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoCard("20:00", R.drawable.ic_relogio)
                InfoCard("14/03/2025", R.drawable.ic_calendar_unselected)
                InfoCard("48", R.drawable.ic_profile_unselected)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "O Pastor convida todas as mulheres á participarem desse evento magnifico, " +
                        "Venha você também e confirme sua presença.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Confirmar Presença */ },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.run { buttonColors(AzulBotao) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Confirmar Presença", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_enviar),
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }

    @Composable
    fun InfoCard(text: String, iconRes: Int) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = text, color = Color.White)
        }
    }


@Preview(showBackground = true)
@Composable
fun PresenceScreenPreview() {
    PresenceScreen()
}