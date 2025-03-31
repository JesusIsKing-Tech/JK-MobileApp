import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.jkconect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    val isFormValid = email.isNotBlank() && senha.isNotBlank()

    // Cores atualizadas
    val primaryColor = Color(0xFF0E48AF)      // Azul escuro
    val backgroundColor = Color(0xFF1C1D21)   // Cinza muito escuro
    val surfaceColor = Color(0xFF2A2B30)      // Um pouco mais claro que o fundo
    val textColor = Color(0xFFF5F5F5)         // Quase branco
    val secondaryTextColor = Color(0xFFBBBBBB) // Cinza claro
    val linkColor = Color(0xFF64B5F6)         // Azul mais claro para links

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
            verticalArrangement = Arrangement.SpaceEvenly // Distribui os elementos igualmente
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
                        value = email,
                        onValueChange = { email = it },
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
                        value = senha,
                        onValueChange = { senha = it },
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

            // Botão de entrar
            Button(
                onClick = { /* Ação de login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = isFormValid
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
                    modifier = Modifier.clickable { /* Ação para criar conta */ }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}