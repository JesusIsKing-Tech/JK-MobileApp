package com.example.jkconect.main.profile

import AddBottomItem
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
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
import androidx.core.content.ContextCompat
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
fun ProfileScreen(perfilViewModel: PerfilViewModel, userId: Int) {
    val state = perfilViewModel.perfilUiState.collectAsState().value
    val profileImageBitmap = perfilViewModel.profileImageBitmap.collectAsState().value
    val context = LocalContext.current

    fun checkAndRequestPermissions() {
        val readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            // Request permission here using ActivityResultLauncher for permissions
            // This part requires more setup in your Activity or a custom Composable
            // For simplicity, I'm omitting the permission request code, but it's crucial.
            // You would typically use rememberLauncherForActivityResult with ActivityResultContracts.RequestPermission()
            // and handle the result.
            println("Permissão de leitura não concedida!")
        }
    }


    LaunchedEffect(userId) { // Observa o userId e busca o perfil quando ele muda e é válido
        if (userId != -1) {
            perfilViewModel.buscarPerfil(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        if (state.isUploading) {
            CircularProgressIndicator(color = Color.White)
            if (!state.uploadError.isNullOrEmpty()) {
                Text(text = "Erro no upload: ${state.uploadError}", color = Color.Red)
            }
        } else if (state.isDeleting) {
            CircularProgressIndicator(color = Color.Red)
            if (!state.deleteError.isNullOrEmpty()) {
                Text(text = "Erro ao remover: ${state.deleteError}", color = Color.Red)
            }
        } else if (state.isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (state.error != null) {
            Text(text = "Erro: ${state.error}", color = Color.Red)
        } else if (state.usuario != null) {
            ProfilePicture(
                profileImageBitmap = profileImageBitmap,
                onImageSelected = { uri ->
                    perfilViewModel.uploadProfilePicture(userId, uri)
                },
                onRemoveClicked = {
                    perfilViewModel.deleteProfilePicture(userId)
                },
                showRemoveButton = state.usuario.foto_perfil_url != null && state.usuario.foto_perfil_url.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(10.dp))
            ProfileField("Nome", state.usuario.nome ?: "")
            ProfileField("Email", state.usuario.email ?: "")
            ProfileField("Data de Nascimento", state.usuario.data_nascimento ?: "")
            ProfileField("Número", state.usuario.telefone ?: "")
            state.usuario?.endereco?.let { endereco ->
                val logradouro = endereco.logradouro ?: ""
                val numero = endereco.numero ?: ""

                val enderecoCompleto = if (logradouro.isNotEmpty() && numero.isNotEmpty()) {
                    "$logradouro, $numero"
                } else if (logradouro.isNotEmpty()) {
                    logradouro
                } else if (numero.isNotEmpty()) {
                    numero
                } else {
                    "Não informado"
                }
                ProfileField("Endereço", enderecoCompleto)
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
fun ProfilePicture(
    profileImageBitmap: ImageBitmap? = null,
    onImageSelected: (Uri) -> Unit,
    onRemoveClicked: () -> Unit,
    showRemoveButton: Boolean = false
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            result.data?.data?.let { uri ->
                onImageSelected(uri)
            }
        }
    }

    Box(contentAlignment = Alignment.BottomCenter) { // Mantém o alinhamento inferior central
        val painter: Painter = if (profileImageBitmap != null) {
            androidx.compose.ui.graphics.painter.BitmapPainter(profileImageBitmap)
        } else {
            painterResource(id = R.drawable.ic_profile_unselected)
        }

        Image(
            painter = painter,
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(1.dp, PurpleGrey80, CircleShape),
            contentScale = ContentScale.Crop
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    imagePickerLauncher.launch(intent)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Foto",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
            if (showRemoveButton) {
                IconButton(
                    onClick = onRemoveClicked,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover Foto",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red
                    )
                }
            }
        }
    }}

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
                .background(AzulClarinho.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
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


