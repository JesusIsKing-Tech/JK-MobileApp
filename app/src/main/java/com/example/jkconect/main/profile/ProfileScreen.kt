package com.example.jkconect.main.profile

import AddBottomItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.jkconect.R
import com.example.jkconect.navigation.item.BottomNavItem
import com.example.jkconect.navigation.navhost.CalendarScreenRoute
import com.example.jkconect.navigation.navhost.FeedScreenRoute
import com.example.jkconect.navigation.navhost.MyEventsScreenRoute
import com.example.jkconect.navigation.navhost.ProfileScreenRoute
import com.example.jkconect.ui.theme.AlphaPrimaryColor
import com.example.jkconect.ui.theme.AzulClarinho
import com.example.jkconect.ui.theme.CinzaEscuroFundo
import com.example.jkconect.ui.theme.PrimaryColor
import com.example.jkconect.ui.theme.PurpleGrey40
import com.example.jkconect.ui.theme.PurpleGrey80
// import com.example.jkconect.main.navigation.BottomNavItem
//import com.example.jkconect.main.navigation.AddBottomItem
// import com.example.jkconect.main.navigation.FeedScreenRoute
// import com.example.jkconect.main.navigation.CalendarScreenRoute
// import com.example.jkconect.main.navigation.MyEventsScreenRoute
// import com.example.jkconect.ui.theme.AlphaPrimaryColor

@Composable
fun ProfileScreen(navHostController: NavHostController = rememberNavController()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CinzaEscuroFundo)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(15.dp))
                ProfilePicture()
                Spacer(modifier = Modifier.height(10.dp))
                ProfileField("Nome", "Victor da Silva Pereira")
                ProfileField("Email", "victorsilva@gmail.com")
                ProfileField("Data de Nascimento", "16/01/2005")
                ProfileField("Número", "+55 (11) 97732-2577")
                ProfileField("Endereço", "Rua Haddock Lobo 595, Consoloção")
                Spacer(modifier = Modifier.height(15.dp))
                FamilySection()
            }
        }


@Composable
fun ProfilePicture() {
    Box(contentAlignment = Alignment.BottomEnd) {
        Image(
            painter = painterResource(id = R.drawable.photo_mulher_perfil),
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(115.dp)
                .clip(CircleShape)
                .border(2.dp, PurpleGrey80, CircleShape),
            contentScale = ContentScale.Crop
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_editar_perfil),
            contentDescription = "Editar",
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.BottomEnd),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(text = label, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        BasicTextField(
            value = value,
            onValueChange = {},
            textStyle = TextStyle(PurpleGrey40, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulClarinho, shape = MaterialTheme.shapes.small)
                .padding(8.dp)
        )
    }
}

@Composable
fun FamilySection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(bottom = 10.dp))
        Text(
            text = "Sua Família",
            color = Color.White,
            fontSize = 25.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Nome",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Data de nasc",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )}

        FamilyMember("Lucas da Silva",  "07/09/2001")
        FamilyMember("Rosana da Silva", "17/02/1989")
        FamilyMember("José da Silva",  "19/02/1950")
        FamilyMember("Silva da Silva",  "20/08/1999")
    }
}

@Composable
fun FamilyMember(name: String, birthdate: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = Color.White, fontSize = 14.sp,textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Text(birthdate, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
    }
    Divider(color = Color.Gray, thickness = 1.dp)
}




@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}