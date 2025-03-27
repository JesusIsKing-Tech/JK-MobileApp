import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jkconect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroScreen() {
    // Estado para controlar a etapa atual
    var etapaAtual by remember { mutableStateOf(1) }

    // Estados para os campos da etapa 1
    var email by remember { mutableStateOf("") }
    var confirmaEmail by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmaSenha by remember { mutableStateOf("") }

    // Estados para os campos da etapa 2
    var nomeCompleto by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var mes by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var rua by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var uf by remember { mutableStateOf("") }

    // Estado para a etapa 3
    var precisaDoacao by remember { mutableStateOf<Boolean?>(null) }

    // Validação da etapa 1
    val etapa1Valida = email.isNotBlank() && confirmaEmail.isNotBlank() &&
            senha.isNotBlank() && confirmaSenha.isNotBlank()

    // Validação da etapa 2
    val etapa2Valida = nomeCompleto.isNotBlank()

    // Validação da etapa 3
    val etapa3Valida = precisaDoacao != null

    // Cores
    val primaryColor = Color(0xFF0E48AF)      // Azul escuro
    val backgroundColor = Color(0xFF1C1D21)   // Cinza muito escuro
    val surfaceColor = Color(0xFF2A2B30)      // Um pouco mais claro que o fundo
    val textColor = Color(0xFFF5F5F5)         // Quase branco
    val secondaryTextColor = Color(0xFFBBBBBB) // Cinza claro
    val successColor = Color(0xFF4CAF50)      // Verde para "Sim"
    val dangerColor = Color(0xFFE53935)       // Vermelho para "Não"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabeçalho fixo com botão de voltar e indicador de progresso
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = backgroundColor,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Botão de voltar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (etapaAtual > 1) {
                                    etapaAtual--
                                } else {
                                    // Voltar para a tela de login
                                }
                            },
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Voltar",
                                tint = primaryColor
                            )
                        }
                    }

                    // Indicador de progresso
                    ProgressIndicator(
                        currentStep = etapaAtual,
                        totalSteps = 3,
                        primaryColor = primaryColor
                    )
                }
            }

            // Conteúdo rolável
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Conteúdo baseado na etapa atual
                when (etapaAtual) {
                    1 -> {
                        EtapaCredenciais(
                            email = email,
                            onEmailChange = { email = it },
                            confirmaEmail = confirmaEmail,
                            onConfirmaEmailChange = { confirmaEmail = it },
                            senha = senha,
                            onSenhaChange = { senha = it },
                            confirmaSenha = confirmaSenha,
                            onConfirmaSenhaChange = { confirmaSenha = it },
                            onNextClick = { etapaAtual = 2 },
                            isNextEnabled = etapa1Valida,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            surfaceColor = surfaceColor
                        )
                    }
                    2 -> {
                        EtapaDadosPessoais(
                            nomeCompleto = nomeCompleto,
                            onNomeCompletoChange = { nomeCompleto = it },
                            genero = genero,
                            onGeneroChange = { genero = it },
                            dia = dia,
                            onDiaChange = { dia = it },
                            mes = mes,
                            onMesChange = { mes = it },
                            ano = ano,
                            onAnoChange = { ano = it },
                            cep = cep,
                            onCepChange = { cep = it },
                            rua = rua,
                            onRuaChange = { rua = it },
                            numero = numero,
                            onNumeroChange = { numero = it },
                            bairro = bairro,
                            onBairroChange = { bairro = it },
                            cidade = cidade,
                            onCidadeChange = { cidade = it },
                            uf = uf,
                            onUfChange = { uf = it },
                            onNextClick = { etapaAtual = 3 },
                            isNextEnabled = etapa2Valida,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            surfaceColor = surfaceColor
                        )
                    }
                    3 -> {
                        EtapaDoacao(
                            precisaDoacao = precisaDoacao,
                            onPrecisaDoacaoChange = { precisaDoacao = it },
                            onCadastrarClick = { /* Implementar cadastro */ },
                            isCadastrarEnabled = etapa3Valida,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            successColor = successColor,
                            dangerColor = dangerColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            // Círculo
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (i <= currentStep) primaryColor else Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (i <= currentStep) {
                    Text(
                        text = i.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Linha conectora (exceto após o último círculo)
            if (i < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (i < currentStep) primaryColor
                            else Color.LightGray
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtapaCredenciais(
    email: String,
    onEmailChange: (String) -> Unit,
    confirmaEmail: String,
    onConfirmaEmailChange: (String) -> Unit,
    senha: String,
    onSenhaChange: (String) -> Unit,
    confirmaSenha: String,
    onConfirmaSenhaChange: (String) -> Unit,
    onNextClick: () -> Unit,
    isNextEnabled: Boolean,
    primaryColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Título
        Text(
            text = "CADASTRO CREDENCIAIS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        // Campos de entrada
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo de email
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email", color = textColor) },
                placeholder = { Text("Digite seu email", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = primaryColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = surfaceColor,
                    focusedPlaceholderColor = secondaryTextColor,
                    unfocusedPlaceholderColor = secondaryTextColor
                )
            )

            // Campo de confirmação de email
            OutlinedTextField(
                value = confirmaEmail,
                onValueChange = onConfirmaEmailChange,
                label = { Text("Confirmação de Email", color = textColor) },
                placeholder = { Text("Confirme seu email", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = primaryColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = surfaceColor,
                    focusedPlaceholderColor = secondaryTextColor,
                    unfocusedPlaceholderColor = secondaryTextColor
                )
            )

            // Campo de senha (sempre visível)
            OutlinedTextField(
                value = senha,
                onValueChange = onSenhaChange,
                label = { Text("Senha", color = textColor) },
                placeholder = { Text("Digite sua senha", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = primaryColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = surfaceColor,
                    focusedPlaceholderColor = secondaryTextColor,
                    unfocusedPlaceholderColor = secondaryTextColor
                )
            )

            // Campo de confirmação de senha (sempre visível)
            OutlinedTextField(
                value = confirmaSenha,
                onValueChange = onConfirmaSenhaChange,
                label = { Text("Confirmação de Senha", color = textColor) },
                placeholder = { Text("Confirme sua senha", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = primaryColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = surfaceColor,
                    focusedPlaceholderColor = secondaryTextColor,
                    unfocusedPlaceholderColor = secondaryTextColor
                )
            )
        }

        // Botão de próxima etapa
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White,
                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = isNextEnabled
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Próxima Etapa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtapaDadosPessoais(
    nomeCompleto: String,
    onNomeCompletoChange: (String) -> Unit,
    genero: String,
    onGeneroChange: (String) -> Unit,
    dia: String,
    onDiaChange: (String) -> Unit,
    mes: String,
    onMesChange: (String) -> Unit,
    ano: String,
    onAnoChange: (String) -> Unit,
    cep: String,
    onCepChange: (String) -> Unit,
    rua: String,
    onRuaChange: (String) -> Unit,
    numero: String,
    onNumeroChange: (String) -> Unit,
    bairro: String,
    onBairroChange: (String) -> Unit,
    cidade: String,
    onCidadeChange: (String) -> Unit,
    uf: String,
    onUfChange: (String) -> Unit,
    onNextClick: () -> Unit,
    isNextEnabled: Boolean,
    primaryColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Título
        Text(
            text = "CADASTRO DADOS PESSOAIS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        // Campos de entrada
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nome Completo
            OutlinedTextField(
                value = nomeCompleto,
                onValueChange = onNomeCompletoChange,
                label = { Text("Nome Completo", color = textColor) },
                placeholder = { Text("Digite seu nome completo", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = primaryColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = surfaceColor,
                    focusedPlaceholderColor = secondaryTextColor,
                    unfocusedPlaceholderColor = secondaryTextColor
                )
            )

            // Data de nascimento
            Text(
                text = "Data de nascimento",
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dia
                OutlinedTextField(
                    value = dia,
                    onValueChange = onDiaChange,
                    label = { Text("Dia", color = textColor) },
                    placeholder = { Text("DD", color = secondaryTextColor) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )

                // Mês
                OutlinedTextField(
                    value = mes,
                    onValueChange = onMesChange,
                    label = { Text("Mês", color = textColor) },
                    placeholder = { Text("MM", color = secondaryTextColor) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )

                // Ano
                OutlinedTextField(
                    value = ano,
                    onValueChange = onAnoChange,
                    label = { Text("Ano", color = textColor) },
                    placeholder = { Text("AAAA", color = secondaryTextColor) },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )
            }

            // CEP
            OutlinedTextField(
                value = cep,
                onValueChange = onCepChange,
                label = { Text("CEP", color = textColor) },
                placeholder = { Text("00000-000", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = primaryColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = surfaceColor,
                    focusedPlaceholderColor = secondaryTextColor,
                    unfocusedPlaceholderColor = secondaryTextColor
                )
            )

            // Rua e Número
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rua
                OutlinedTextField(
                    value = rua,
                    onValueChange = onRuaChange,
                    label = { Text("Rua", color = textColor) },
                    placeholder = { Text("Digite o nome da rua", color = secondaryTextColor) },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )

                // Número
                OutlinedTextField(
                    value = numero,
                    onValueChange = onNumeroChange,
                    label = { Text("Número", color = textColor) },
                    placeholder = { Text("No.", color = secondaryTextColor) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )
            }

            // Bairro, Cidade e UF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bairro
                OutlinedTextField(
                    value = bairro,
                    onValueChange = onBairroChange,
                    label = { Text("Bairro", color = textColor) },
                    placeholder = { Text("Digite o bairro", color = secondaryTextColor) },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )

                // Cidade
                OutlinedTextField(
                    value = cidade,
                    onValueChange = onCidadeChange,
                    label = { Text("Cidade", color = textColor) },
                    placeholder = { Text("Digite a cidade", color = secondaryTextColor) },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )

                // UF
                OutlinedTextField(
                    value = uf,
                    onValueChange = onUfChange,
                    label = { Text("UF", color = textColor) },
                    placeholder = { Text("UF", color = secondaryTextColor) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedPlaceholderColor = secondaryTextColor,
                        unfocusedPlaceholderColor = secondaryTextColor
                    )
                )
            }
        }

        // Botão de próxima etapa
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White,
                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = isNextEnabled
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Próxima Etapa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EtapaDoacao(
    precisaDoacao: Boolean?,
    onPrecisaDoacaoChange: (Boolean) -> Unit,
    onCadastrarClick: () -> Unit,
    isCadastrarEnabled: Boolean,
    primaryColor: Color,
    textColor: Color,
    successColor: Color,
    dangerColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Título
        Text(
            text = "PRECISAMOS SABER",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        // Pergunta sobre doação
        Text(
            text = "Você precisa de doação de alimentos?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )

        // Botões de Sim e Não estilizados
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botão SIM
            Button(
                onClick = { onPrecisaDoacaoChange(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (precisaDoacao == true) successColor else Color.Gray.copy(alpha = 0.3f),
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Sim",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "SIM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botão NÃO
            Button(
                onClick = { onPrecisaDoacaoChange(false) },
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (precisaDoacao == false) dangerColor else Color.Gray.copy(alpha = 0.3f),
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Não",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "NÃO",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Texto explicativo
        Text(
            text = "Sua resposta nos ajudará a direcionar recursos para quem mais precisa.",
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        // Botão de cadastrar
        Button(
            onClick = onCadastrarClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White,
                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = isCadastrarEnabled
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cadastrar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    CadastroScreen()
}