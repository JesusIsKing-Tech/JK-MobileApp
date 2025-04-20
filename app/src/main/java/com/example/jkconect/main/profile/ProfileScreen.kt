package com.example.jkconect.main.profile

import AddBottomItem
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.example.jkconect.ui.theme.JKConectTheme

import com.example.jkconect.viewmodel.PerfilViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun ProfileScreen(perfilViewModel: PerfilViewModel) {
    val state = perfilViewModel.perfilUiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        if (state.isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (state.error != null) {
            Text(text = "Erro: ${state.error}", color = Color.Red)
        } else if (state.usuario != null) {
            ProfilePicture(profileImageUrl = state.usuario.foto_perfil_url)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileField("Nome", state.usuario.nome ?: "")
            ProfileField("Email", state.usuario.email ?: "")
            ProfileField("Data de Nascimento", state.usuario.data_nascimento ?: "")
            ProfileField("Número", state.usuario.telefone ?: "")
            state.usuario.endereco?.let { endereco ->
                Text(
                    text = "Endereço:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.White
                )
                ProfileField("Logradouro", endereco.logradouro ?: "Não informado")
                ProfileField("Número", endereco.numero ?: "Não informado")
            } ?: run {
                ProfileField("Endereço", "Não informado")
            }
            Spacer(modifier = Modifier.height(15.dp))
            // FamilySection(familia = state.usuario.familia)
        } else {
            Text(text = "Nenhum perfil encontrado.", color = Color.White)
        }
    }
}

@Composable
fun ProfilePicture(profileImageUrl: String? = null) {
    Box(contentAlignment = Alignment.BottomEnd) {
        val painter = rememberImagePainter(
            data = profileImageUrl ?: R.drawable.photo_mulher_perfil,
            builder = {
                crossfade(true)
            }
        )
        Image(
            painter = painter,
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
                .padding(8.dp),
            readOnly = true
        )
    }
}

/*
@Composable
fun FamilySection(familia: List<FamilyMember>? = null) {
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
                text = "Data de Nascimento",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }

        familia?.forEach { member ->
            FamilyMember(name = member.nome, birthdate = member.dataNascimento)
        } ?: Text("Nenhum membro da família encontrado.", color = Color.White)
    }
}

@Composable
fun FamilyMember(name: String, birthdate: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Text(birthdate, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
    }
    Divider(color = Color.Gray, thickness = 1.dp)
}

data class FamilyMember(val nome: String, val dataNascimento: String)
*/


