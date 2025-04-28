import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jkconect.R
import com.example.jkconect.viewmodel.LoginViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreen(navController: NavHostController, onLoginSuccess: (String, Int) -> Unit) {
    val viewModel: LoginViewModel = koinViewModel()
    val state = viewModel.loginUiState.collectAsState().value

    Log.d("LoginScreen", "ESSE É O STATE ${state.token}, USER ID: ${state.userId}")

    var senhaVisivel by remember { mutableStateOf(false) }
    val isFormValid = state.email.isNotBlank() && state.senha.isNotBlank()

    // Cores atualizadas
    val primaryColor = Color(0xFF0E48AF)
    val backgroundColor = Color(0xFF1C1D21)
    val surfaceColor = Color(0xFF2A2B30)
    val textColor = Color(0xFFF5F5F5)
    val secondaryTextColor = Color(0xFFBBBBBB)
    val linkColor = Color(0xFF64B5F6)

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo2),
                    contentDescription = "Logo",
                    modifier = Modifier.size(138.dp)
                )
            }

            // Mensagem de boas-vindas
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Bem-vindo à PIBVM!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Confirme seus dados para continuar",
                    fontSize = 20.sp,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center
                )
            }

            // Formulário
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Campo de e-mail
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "E-mail",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        placeholder = { Text("Digite seu e-mail") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp, color = textColor),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            disabledContainerColor = surfaceColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF4A4B50),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = primaryColor,
                            focusedPlaceholderColor = secondaryTextColor,
                            unfocusedPlaceholderColor = secondaryTextColor
                        )
                    )
                }

                // Campo de senha
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Senha",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )

                    OutlinedTextField(
                        value = state.senha,
                        onValueChange = { viewModel.onSenhaChanged(it) },
                        placeholder = { Text("Digite sua senha") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp, color = textColor),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            disabledContainerColor = surfaceColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF4A4B50),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = primaryColor,
                            focusedPlaceholderColor = secondaryTextColor,
                            unfocusedPlaceholderColor = secondaryTextColor
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { senhaVisivel = !senhaVisivel }
                            ) {
                                Image(
                                    painter = painterResource(id = if (senhaVisivel) R.drawable.ic_visualizar else R.drawable.ic_visualizado ),
                                    contentDescription = "senha",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                            }
                        }
                    )
                }
            }

            // Link "Esqueceu a senha?"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Esqueceu a senha?",
                    fontSize = 18.sp,
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { /* Ação para recuperar senha */ }
                )
            }

            LaunchedEffect(state.token) {
                if (!state.token.isNullOrBlank() && state.userId != null) {
                    Log.d("LoginScreen", "Token atualizado no estado: ${state.token}, UserId: ${state.userId}")
                    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    try {
                        sharedPreferences.edit().putString("jwt_token", state.token).putInt("userId", state.userId).apply()
                        onLoginSuccess(state.token, state.userId) // Passa o userId para o callback
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Erro ao salvar SharedPreferences: ${e.javaClass.name} - ${e.localizedMessage}")
                    }
                } else if (state.error != null) {
                    Log.e("LoginScreen", "Erro de login detectado no estado: ${state.error}")
                }
            }

            Button(
                onClick = {
                    viewModel.login(
                        onSuccess = { userId ->
                            Log.d("LoginScreen", "Login bem-sucedido. UserId recebido: $userId")
                            Log.d("LoginScreen", "Valor de state.token antes de salvar: ${state.token}")
                            Log.d("LoginScreen", "Valor de userId antes de salvar: $userId")

                        },
                        onError = { errorMessage ->
                            Log.e("LoginScreen", "Erro de login: $errorMessage")
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = isFormValid && !state.isLoading
            ) {
                Text(
                    text = "Entrar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Link para criar conta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Não tem uma conta? ",
                    fontSize = 18.sp,
                    color = secondaryTextColor
                )
                Text(
                    text = "Criar conta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { navController.navigate("cadastro") }
                )
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (state.error != null) {
            // TODO: Exibir mensagem de erro de forma mais amigável
            Text(text = "Erro: ${state.error}", color = Color.Red, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(navController = NavHostController(LocalContext.current), onLoginSuccess = { _, _ -> })
}