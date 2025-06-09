package com.example.jkconect.main.profile

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jkconect.R
import com.example.jkconect.model.UsuarioResponseDto
import com.example.jkconect.viewmodel.PerfilViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    userId: Int,
    onLogout: () -> Unit, // Callback para logout
    perfilViewModel: PerfilViewModel = koinViewModel()
) {
    val state = perfilViewModel.perfilUiState.collectAsState().value
    val profileImageBitmap = perfilViewModel.profileImageBitmap.collectAsState().value
    val context = LocalContext.current

    // Estado para o checkbox de doações
    var receberDoacoes by remember { mutableStateOf(state.usuario?.receber_doacoes ?: false) }

    // Cores do tema conforme solicitado
    val backgroundColor = Color(0xFF1C1D21)
    val primaryColor = Color(0xFF3B5FE9)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFAAAAAA)
    val cardColor = Color(0xFF2A2B30)
    val buttonInactiveColor = Color(0xFF808080)
    val accentColor = primaryColor
    val dangerColor = Color(0xFFE53935)

    // Carregar perfil quando o userId mudar
    LaunchedEffect(userId) {
        if (userId != -1) {
            Log.d("ProfileScreen", "Carregando perfil para usuário $userId")
            perfilViewModel.buscarPerfil(userId)
            perfilViewModel.buscarFamilia(userId)
        }
    }

    // Atualizar estado do checkbox quando o usuário for carregado
    LaunchedEffect(state.usuario?.receber_doacoes) {
        receberDoacoes = state.usuario?.receber_doacoes ?: false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.95f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header com título e botão de logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meu Perfil",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .background(
                            color = dangerColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sair",
                        tint = dangerColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            when {
                state.isLoading -> {
                    LoadingSection(primaryColor)
                }
                state.error != null -> {
                    ErrorSection(
                        error = state.error,
                        onRetry = { perfilViewModel.buscarPerfil(userId) },
                        primaryColor = primaryColor
                    )
                }
                state.usuario != null -> {
                    // Card da foto de perfil
                    ProfilePictureCard(
                        profileImageBitmap = profileImageBitmap,
                        userName = state.usuario.nome ?: "Usuário",
                        userEmail = state.usuario.email ?: "",
                        onImageSelected = { uri ->
                            perfilViewModel.uploadProfilePicture(userId, uri)
                        },
                        onRemoveClicked = {
                            perfilViewModel.deleteProfilePicture(userId)
                        },
                        showRemoveButton = state.usuario.foto_perfil_url != null && state.usuario.foto_perfil_url.isNotEmpty(),
                        isUploading = state.isUploading,
                        isDeleting = state.isDeleting,
                        cardColor = cardColor,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Card de informações pessoais
                    PersonalInfoCard(
                        usuario = state.usuario,
                        cardColor = cardColor,
                        textPrimary = textColor,
                        textSecondary = secondaryTextColor,
                        accentColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card de preferências
                    PreferencesCard(
                        receberDoacoes = receberDoacoes,
                        onDoacoesChanged = { receberDoacoes = it },
                        cardColor = cardColor,
                        textPrimary = textColor,
                        textSecondary = secondaryTextColor,
                        accentColor = primaryColor,
                        userId = userId,
                        perfilViewModel = perfilViewModel
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card da família
                    FamilyCard(
                        familia = state.familia,
                        isFamiliaLoading = state.isFamiliaLoading,
                        familiaError = state.familiaError,
                        cardColor = cardColor,
                        textPrimary = textColor,
                        textSecondary = secondaryTextColor,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(100.dp)) // Espaço para bottom navigation
                }
                else -> {
                    EmptyStateSection(
                        onRetry = { perfilViewModel.buscarPerfil(userId) },
                        primaryColor = primaryColor,
                        textPrimary = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun ProfilePictureCard(
    profileImageBitmap: ImageBitmap?,
    userName: String,
    userEmail: String,
    onImageSelected: (Uri) -> Unit,
    onRemoveClicked: () -> Unit,
    showRemoveButton: Boolean,
    isUploading: Boolean,
    isDeleting: Boolean,
    cardColor: Color,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                ProfilePictureWithActions(
                    profileImageBitmap = profileImageBitmap,
                    onImageSelected = onImageSelected,
                    onRemoveClicked = onRemoveClicked,
                    showRemoveButton = showRemoveButton,
                    primaryColor = primaryColor
                )

                if (isUploading || isDeleting) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfilePictureWithActions(
    profileImageBitmap: ImageBitmap?,
    onImageSelected: (Uri) -> Unit,
    onRemoveClicked: () -> Unit,
    showRemoveButton: Boolean,
    primaryColor: Color
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            result.data?.data?.let { uri ->
                onImageSelected(uri)
            }
        }
    }

    Box(contentAlignment = Alignment.BottomEnd) {
        val painter: Painter = if (profileImageBitmap != null) {
            androidx.compose.ui.graphics.painter.BitmapPainter(profileImageBitmap)
        } else {
            painterResource(id = R.drawable.ic_profile_unselected)
        }

        Image(
            painter = painter,
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(4.dp, primaryColor, CircleShape),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier.offset(x = 8.dp, y = 8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    imagePickerLauncher.launch(intent)
                },
                modifier = Modifier.size(40.dp),
                containerColor = primaryColor
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Foto",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (showRemoveButton) {
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = onRemoveClicked,
                    modifier = Modifier.size(40.dp),
                    containerColor = Color(0xFFE53935)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover Foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalInfoCard(
    usuario: com.example.jkconect.model.Usuario,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Informações Pessoais",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            InfoField(
                icon = Icons.Default.Person,
                label = "Nome",
                value = usuario.nome ?: "Não informado",
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )

            InfoField(
                icon = Icons.Default.Email,
                label = "Email",
                value = usuario.email ?: "Não informado",
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )

            InfoField(
                icon = Icons.Default.DateRange,
                label = "Data de Nascimento",
                value = usuario.data_nascimento ?: "Não informado",
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )

            InfoField(
                icon = Icons.Default.Phone,
                label = "Telefone",
                value = usuario.telefone ?: "Não informado",
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )

            val enderecoCompleto = usuario.endereco?.let { endereco ->
                val logradouro = endereco.logradouro ?: ""
                val numero = endereco.numero ?: ""
                when {
                    logradouro.isNotEmpty() && numero.isNotEmpty() -> "$logradouro, $numero"
                    logradouro.isNotEmpty() -> logradouro
                    numero.isNotEmpty() -> numero
                    else -> "Não informado"
                }
            } ?: "Não informado"

            InfoField(
                icon = Icons.Default.LocationOn,
                label = "Endereço",
                value = enderecoCompleto,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                isLast = true
            )
        }
    }
}

@Composable
fun PreferencesCard(
    receberDoacoes: Boolean,
    onDoacoesChanged: (Boolean) -> Unit,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    userId: Int,
    perfilViewModel: PerfilViewModel
) {
    val state = perfilViewModel.perfilUiState.collectAsState().value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Preferências",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.isUpdatingPreferences) {
                        val newValue = !receberDoacoes
                        onDoacoesChanged(newValue)
                        perfilViewModel.atualizarPreferenciasDoacao(userId, newValue)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBasket,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Desejo Receber Doações",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    Text(
                        text = "Fique atento ao calendário da igreja para a retirada de doações",
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                }

                if (state.isUpdatingPreferences) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = accentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Switch(
                        checked = receberDoacoes,
                        onCheckedChange = { newValue ->
                            onDoacoesChanged(newValue)
                            perfilViewModel.atualizarPreferenciasDoacao(userId, newValue)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF505050)
                        )
                    )
                }
            }

            // Mostrar mensagem de sucesso
            if (state.updatePreferencesSuccess) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Preferência atualizada com sucesso!",
                        color = Color.Green,
                        fontSize = 12.sp
                    )
                }
            }

            // Mostrar erro se houver
            if (state.updatePreferencesError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Erro: ${state.updatePreferencesError}",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}




@Composable
fun FamilyCard(
    familia: List<UsuarioResponseDto>?,
    isFamiliaLoading: Boolean,
    familiaError: String?,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Minha Família",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            when {
                isFamiliaLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                familiaError != null -> {
                    Text(
                        text = "Erro ao carregar família: $familiaError",
                        color = Color(0xFFE53935),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                familia.isNullOrEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nenhum membro da família encontrado",
                            color = textSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    familia.forEachIndexed { index, member ->
                        FamilyMemberItem(
                            name = member.nome ?: "Não informado",
                            birthdate = member.data_nascimento ?: "Não informado",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            isLast = index == familia.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoField(
    icon: ImageVector,
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color,
    isLast: Boolean = false
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = textPrimary
                )
            }
        }
        if (!isLast) {
            HorizontalDivider(
                color = Color(0xFF3A3B40),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun FamilyMemberItem(
    name: String,
    birthdate: String,
    textPrimary: Color,
    textSecondary: Color,
    isLast: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                Text(
                    text = birthdate,
                    fontSize = 14.sp,
                    color = textSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        if (!isLast) {
            HorizontalDivider(
                color = Color(0xFF3A3B40),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun LoadingSection(primaryColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(64.dp)
    ) {
        CircularProgressIndicator(
            color = primaryColor,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Carregando perfil...",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ErrorSection(
    error: String,
    onRetry: () -> Unit,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = Color(0xFFE53935),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Erro: $error",
            color = Color(0xFFE53935),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Tentar novamente", color = Color.White)
        }
    }
}

@Composable
fun EmptyStateSection(
    onRetry: () -> Unit,
    primaryColor: Color,
    textPrimary: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color(0xFF808080),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum perfil encontrado",
            color = textPrimary,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Carregar perfil", color = Color.White)
        }
    }
}
