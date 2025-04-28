import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Cores personalizadas para o tema da igreja
val PrimaryColor = Color(0xFF3F51B5) // Azul índigo
val SecondaryColor = Color(0xFF7986CB) // Azul índigo mais claro
val AccentColor = Color(0xFFFF9800) // Laranja
val BackgroundColor = Color(0xFFF5F5F5) // Cinza muito claro
val SurfaceColor = Color.White
val TextPrimaryColor = Color(0xFF212121) // Quase preto
val TextSecondaryColor = Color(0xFF757575) // Cinza
val DividerColor = Color(0xFFBDBDBD) // Cinza claro
val SuccessColor = Color(0xFF4CAF50) // Verde
val ErrorColor = Color(0xFFF44336) // Vermelho

// Modelos de dados para os formulários
data class PedidoOracao(
    val nome: String,
    val pedido: String,
    val dataHora: Long = System.currentTimeMillis(),
    val status: StatusPedido = StatusPedido.PENDENTE
)

enum class StatusPedido {
    PENDENTE,
    RECEBIDO,
    EM_ORACAO,
    RESPONDIDO
}

data class AtualizacaoEndereco(
    val nome: String,
    val cep: String,
    val rua: String,
    val numero: String,
    val complemento: String,
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
    val dataHora: Long = System.currentTimeMillis()
)

// Modelo para usuário logado
data class UsuarioLogado(
    val id: String,
    val nome: String,
    val email: String,
    val telefone: String = ""
)

// Modelo para informações do pastor
data class InformacaoPastor(
    val nome: String,
    val telefone: String,
    val horarioAtendimento: String
)

// Enum para controlar as diferentes seções do chat
enum class ChatSection {
    MAIN_MENU,
    PERGUNTAS_FREQUENTES,
    PEDIDO_ORACAO,
    ATUALIZACAO_ENDERECO,
    CONTATO_PASTOR
}

// Classe para representar uma pergunta frequente
data class PerguntaFrequente(
    val pergunta: String,
    val palavrasChave: List<String>,
    val resposta: String
)

// Classe para representar uma mensagem no chat
data class Mensagem(
    val texto: String,
    val isUsuario: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = java.util.UUID.randomUUID().toString()
)

// Classe para representar uma notificação
data class Notificacao(
    val titulo: String,
    val mensagem: String,
    val tipo: TipoNotificacao,
    val duracao: Long = 5000,
    val id: String = java.util.UUID.randomUUID().toString()
)

enum class TipoNotificacao {
    SUCESSO,
    ERRO,
    INFO,
    AVISO
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IgrejaChatComponent(
    nomeIgreja: String = "",
    usuarioLogado: UsuarioLogado? = null,
    backendUrl: String = "http://10.0.2.2:80", // Novo parâmetro para URL do backend
    modifier: Modifier = Modifier,
    informacaoPastor: InformacaoPastor = InformacaoPastor(
        nome = "Pastor Raphael Xavier",
        telefone = "5511999999999",
        horarioAtendimento = "Segunda a Sexta, 9h às 17h"
    ),
    onPedidoOracaoEnviado: (PedidoOracao) -> Unit = {},
    onAtualizacaoEnderecoEnviada: (AtualizacaoEndereco) -> Unit = {},
    onBuscarEnderecoPorCep: suspend (String) -> Map<String, String>? = { null },
) {
    // Novo estado para indicar quando a IA está processando
    var iaRespondendo by remember { mutableStateOf(false) }

    // Integração com o Mistral AI
    val mistralAI = remember { MistralAIIntegration(backendUrl) }
    // Estados para controlar o comportamento do chat
    var isExpanded by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf(ChatSection.MAIN_MENU) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var mensagemInput by remember { mutableStateOf("") }

    // Estados para os formulários
    var oracaoPedido by remember { mutableStateOf("") }

    // Estados para atualização de endereço
    var enderecoCep by remember { mutableStateOf("") }
    var enderecoRua by remember { mutableStateOf("") }
    var enderecoNumero by remember { mutableStateOf("") }
    var enderecoComplemento by remember { mutableStateOf("") }
    var enderecoBairro by remember { mutableStateOf("") }
    var enderecoCidade by remember { mutableStateOf("") }
    var enderecoEstado by remember { mutableStateOf("") }
    var buscandoCep by remember { mutableStateOf(false) }

    // Estado para notificações
    val notificacoes = remember { mutableStateListOf<Notificacao>() }

    // Contexto para iniciar intents
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estado para controlar o rolamento automático do chat
    val listState = rememberLazyListState()
    val mensagens = remember { mutableStateListOf<Mensagem>() }

    // Efeito para rolar automaticamente para a última mensagem quando uma nova mensagem é adicionada
    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) {
            listState.animateScrollToItem(mensagens.size - 1)
        }
    }

    // Lista de perguntas frequentes com palavras-chave para reconhecimento
    val perguntasFrequentes = remember {
        listOf(
            PerguntaFrequente(
                pergunta = "Qual o horário dos cultos?",
                palavrasChave = listOf("horário", "culto", "cultos", "missa"),
                resposta = "Os cultos acontecem:\n\n" +
                        "• Domingo: 9h e 18h\n" +
                        "• Quarta-feira: 19h30\n" +
                        "• Sábado: 18h\n\n" +
                        "Venha nos visitar!"
            ),
            PerguntaFrequente(
                pergunta = "Como faço para ser batizado?",
                palavrasChave = listOf("batismo", "batizar", "batizado", "batizada"),
                resposta = "Para ser batizado em nossa igreja, você precisa:\n\n" +
                        "1. Participar do curso de batismo (4 aulas)\n" +
                        "2. Ter uma entrevista com um dos pastores\n" +
                        "3. Participar da cerimônia de batismo\n\n" +
                        "O próximo curso começa no dia 15 do próximo mês. Inscreva-se na secretaria da igreja."
            ),
            PerguntaFrequente(
                pergunta = "Onde fica a igreja?",
                palavrasChave = listOf("endereço", "localização", "onde", "fica", "lugar"),
                resposta = "Nossa igreja está localizada na Rua Moreira de Vasconcelos, 425. \n\n"
            ),
            PerguntaFrequente(
                pergunta = "Como faço para doar?",
                palavrasChave = listOf("doar", "doação", "dízimo", "oferta", "contribuir", "pix"),
                resposta = "Você pode apoiar a obra de Deus através da doação de alimentos não perecíveis.\n" +
                        "Aceitamos apenas doações físicas, que podem ser entregues diretamente na portaria da igreja.\n" +
                        "Fique atento às nossas campanhas de arrecadação e saiba que todas as contribuições são usadas com responsabilidade, conforme nosso relatório financeiro mensal disponível no mural da igreja.\n" +
                        "Deus abençoe sua generosidade!"
            ),
            PerguntaFrequente(
                pergunta = "Como falar com o pastor?",
                palavrasChave = listOf("pastor", "conversar", "falar", "contato", "ajuda", "aconselhamento"),
                resposta = "Você pode falar com o ${informacaoPastor.nome} das seguintes formas:\n\n" +
                        "• Pessoalmente: ${informacaoPastor.horarioAtendimento}\n" +
                        "• WhatsApp: Clique no botão 'Falar com o Pastor' no menu principal\n\n" +
                        "Para assuntos urgentes, recomendamos o contato via WhatsApp."
            )
        )
    }

    // Histórico de mensagens do chat

    // Função para adicionar notificação
    fun mostrarNotificacao(titulo: String, mensagem: String, tipo: TipoNotificacao) {
        val notificacao = Notificacao(titulo, mensagem, tipo)
        notificacoes.add(notificacao)

        // Remover a notificação após o tempo definido
        coroutineScope.launch {
            delay(notificacao.duracao)
            notificacoes.remove(notificacao)
        }
    }

    // Função para processar mensagens de entrada e identificar perguntas frequentes
    // Função para processar mensagens de entrada e identificar perguntas frequentes
    fun processarMensagem(texto: String) {
        if (texto.isBlank()) return

        // Adiciona a mensagem do usuário ao histórico
        mensagens.add(Mensagem(texto, true))

        // Limpa o campo de entrada
        mensagemInput = ""
        keyboardController?.hide()

        // Verifica primeiro se a mensagem corresponde a palavras-chave específicas
        // para abrir seções especiais diretamente
        when {
            texto.lowercase().contains("oração") || texto.lowercase().contains("orar") -> {
                currentSection = ChatSection.PEDIDO_ORACAO
                mensagens.add(Mensagem("Por favor, compartilhe seu pedido de oração:", false))
                return
            }
            texto.lowercase().contains("endereço") || texto.lowercase().contains("morada") -> {
                currentSection = ChatSection.ATUALIZACAO_ENDERECO
                mensagens.add(Mensagem("Para atualizar seu endereço, precisamos das seguintes informações:", false))
                mensagens.add(Mensagem("Por favor, informe seu CEP:", false))
                return
            }
            texto.lowercase().contains("pastor") || texto.lowercase().contains("conversar") -> {
                currentSection = ChatSection.CONTATO_PASTOR
                mensagens.add(Mensagem("Você pode falar diretamente com o ${informacaoPastor.nome} através do WhatsApp ou ver os horários de atendimento.", false))
                return
            }
            texto.lowercase().contains("pergunta") || texto.lowercase().contains("faq") -> {
                currentSection = ChatSection.PERGUNTAS_FREQUENTES
                mensagens.add(Mensagem("Escolha uma das perguntas frequentes abaixo:", false))
                return
            }
        }

        // Verifica se a mensagem corresponde a alguma pergunta frequente
        val perguntaEncontrada = perguntasFrequentes.find { pergunta ->
            pergunta.palavrasChave.any { palavra ->
                texto.lowercase().contains(palavra.lowercase())
            }
        }

        if (perguntaEncontrada != null) {
            // Se encontrou uma pergunta correspondente, responde imediatamente
            mensagens.add(Mensagem(perguntaEncontrada.resposta, false))
        } else {
            // Se não encontrou, envia para o Mistral AI

            // Indica que a IA está processando
            iaRespondendo = true

            // Adiciona um indicador de digitação
            val indicadorId = "digitando-${System.currentTimeMillis()}"
            mensagens.add(Mensagem("...", false, id = indicadorId))

            // Envia a mensagem para o Mistral AI
            coroutineScope.launch {
                try {
                    val resposta = mistralAI.enviarMensagem(texto)

                    // Remove o indicador de digitação
                    mensagens.removeAll { it.id == indicadorId }

                    // Adiciona a resposta da IA
                    mensagens.add(Mensagem(resposta, false))
                } catch (e: Exception) {
                    // Remove o indicador de digitação
                    mensagens.removeAll { it.id == indicadorId }

                    // Adiciona mensagem de erro
                    mensagens.add(Mensagem("Desculpe, tive um problema ao processar sua mensagem. Por favor, tente novamente mais tarde.", false))

                    // Mostra notificação de erro
                    mostrarNotificacao(
                        "Erro de Comunicação",
                        "Não foi possível conectar com o assistente virtual: ${e.message}",
                        TipoNotificacao.ERRO
                    )
                } finally {
                    // Finaliza o estado de processamento
                    iaRespondendo = false
                }
            }
        }
    }

    // Função para enviar pedido de oração
    fun enviarPedidoOracao() {
        if (oracaoPedido.isBlank()) {
            mensagens.add(Mensagem("Por favor, compartilhe seu pedido de oração.", false))
            return
        }

        val nomeUsuario = usuarioLogado?.nome ?: "Anônimo"

        // Criar objeto de pedido de oração
        val pedido = PedidoOracao(
            nome = nomeUsuario,
            pedido = oracaoPedido
        )

        // Enviar para o administrador através do callback
        onPedidoOracaoEnviado(pedido)

        // Adicionar confirmação ao chat
        mensagens.add(Mensagem("Seu pedido de oração foi enviado com sucesso! O pastor receberá em breve.", false))

        // Mostrar notificação de sucesso
        mostrarNotificacao(
            "Pedido Enviado",
            "Seu pedido de oração foi recebido e será atendido em breve.",
            TipoNotificacao.SUCESSO
        )

        // Limpar campos e voltar ao menu principal
        oracaoPedido = ""
        currentSection = ChatSection.MAIN_MENU
    }

    // Função para validar e enviar atualização de endereço
    fun validarEnviarAtualizacaoEndereco() {
        if (enderecoCep.isBlank() || enderecoRua.isBlank() ||
            enderecoNumero.isBlank()) {

            var camposFaltantes = mutableListOf<String>()
            if (enderecoCep.isBlank()) camposFaltantes.add("CEP")
            if (enderecoRua.isBlank()) camposFaltantes.add("Rua")
            if (enderecoNumero.isBlank()) camposFaltantes.add("Número")

            val mensagemErro = "Por favor, preencha os seguintes campos obrigatórios: ${camposFaltantes.joinToString(", ")}"
            mensagens.add(Mensagem(mensagemErro, false))

            // Mostrar notificação de erro
            mostrarNotificacao(
                "Campos Obrigatórios",
                mensagemErro,
                TipoNotificacao.ERRO
            )
            return
        }

        val nomeUsuario = usuarioLogado?.nome ?: "Anônimo"

        // Criar objeto de atualização de endereço
        val atualizacao = AtualizacaoEndereco(
            nome = nomeUsuario,
            cep = enderecoCep,
            rua = enderecoRua,
            numero = enderecoNumero,
            complemento = enderecoComplemento,
            bairro = enderecoBairro,
            cidade = enderecoCidade,
            estado = enderecoEstado
        )

        // Enviar para o administrador através do callback
        onAtualizacaoEnderecoEnviada(atualizacao)

        // Adicionar confirmação ao chat
        mensagens.add(Mensagem("Sua solicitação de atualização de endereço foi enviada com sucesso!", false))

        // Mostrar notificação de sucesso
        mostrarNotificacao(
            "Endereço Atualizado",
            "Seu endereço foi atualizado com sucesso: ${enderecoRua}, ${enderecoNumero}",
            TipoNotificacao.SUCESSO
        )

        // Limpar campos e voltar ao menu principal
        enderecoCep = ""
        enderecoRua = ""
        enderecoNumero = ""
        enderecoComplemento = ""
        enderecoBairro = ""
        enderecoCidade = ""
        enderecoEstado = ""
        currentSection = ChatSection.MAIN_MENU
    }

    // Função para buscar endereço por CEP
    fun buscarEnderecoPorCep() {
        if (enderecoCep.length != 8 && enderecoCep.length != 9) {
            mensagens.add(Mensagem("Por favor, digite um CEP válido (8 dígitos).", false))

            // Mostrar notificação de erro
            mostrarNotificacao(
                "CEP Inválido",
                "Por favor, digite um CEP válido com 8 dígitos.",
                TipoNotificacao.ERRO
            )
            return
        }

        coroutineScope.launch {
            buscandoCep = true
            val cepLimpo = enderecoCep.replace("-", "").trim()

            try {
                val resultado = onBuscarEnderecoPorCep(cepLimpo)
                if (resultado != null) {
                    enderecoRua = resultado["logradouro"] ?: ""
                    enderecoBairro = resultado["bairro"] ?: ""
                    enderecoCidade = resultado["localidade"] ?: ""
                    enderecoEstado = resultado["uf"] ?: ""

                    val mensagemSucesso = "CEP encontrado! Endereço: ${enderecoRua}, ${enderecoBairro}, ${enderecoCidade}-${enderecoEstado}"
                    mensagens.add(Mensagem(mensagemSucesso, false))
                    mensagens.add(Mensagem("Agora, por favor, informe o número do seu endereço:", false))

                    // Mostrar notificação de sucesso
                    mostrarNotificacao(
                        "CEP Encontrado",
                        "Endereço localizado com sucesso!",
                        TipoNotificacao.SUCESSO
                    )
                } else {
                    val mensagemErro = "Não foi possível encontrar o endereço com este CEP. Por favor, digite o endereço manualmente."
                    mensagens.add(Mensagem(mensagemErro, false))

                    // Mostrar notificação de erro
                    mostrarNotificacao(
                        "CEP Não Encontrado",
                        mensagemErro,
                        TipoNotificacao.ERRO
                    )
                }
            } catch (e: Exception) {
                val mensagemErro = "Ocorreu um erro ao buscar o CEP. Por favor, digite o endereço manualmente."
                mensagens.add(Mensagem(mensagemErro, false))

                // Mostrar notificação de erro
                mostrarNotificacao(
                    "Erro na Busca",
                    mensagemErro,
                    TipoNotificacao.ERRO
                )
            } finally {
                buscandoCep = false
            }
        }
    }

    // Função para abrir WhatsApp do pastor
   fun abrirWhatsAppPastor() {
        try {
            val telefone = informacaoPastor.telefone
            val mensagemPadrao = "Olá Pastor, gostaria de conversar. Sou ${usuarioLogado?.nome ?: "um membro da igreja"}."

            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$telefone&text=${Uri.encode(mensagemPadrao)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)

            // Mostrar notificação de sucesso
            mostrarNotificacao(
                titulo = "WhatsApp Aberto",
                mensagem = "Conectando com o Pastor ${informacaoPastor.nome}",
                tipo = TipoNotificacao.INFO
            )
        } catch (e: Exception) {
            // Mostrar notificação de erro
            mostrarNotificacao(
                titulo = "Erro ao Abrir WhatsApp",
                mensagem = "Não foi possível abrir o WhatsApp. Verifique se o aplicativo está instalado.",
                tipo = TipoNotificacao.ERRO
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Área de notificações (visível fora do chat)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notificacoes, key = { it.id }) { notificacao ->
                NotificacaoItem(notificacao = notificacao)
            }
        }

        // Animação para o conteúdo do chat
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp)
                .zIndex(1f)
        ) {
            Card(
                modifier = Modifier
                    .width(320.dp)
                    .heightIn(max = 500.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cabeçalho do chat
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(PrimaryColor, SecondaryColor)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = nomeIgreja,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = usuarioLogado?.let { "Olá, ${it.nome}" } ?: "Assistente Virtual",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Área de mensagens com rolamento automático
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = false
                    ) {
                        // Mensagem de boas-vindas inicial
                        if (mensagens.isEmpty()) {
                            item {
                                MensagemItem(
                                    mensagem = Mensagem(
                                        texto = "Olá${usuarioLogado?.let { ", ${it.nome}" } ?: ""}! Como posso ajudar você hoje?",
                                        isUsuario = false
                                    )
                                )
                            }
                        }

                        // Histórico de mensagens
                        items(mensagens, key = { it.id }) { mensagem ->
                            MensagemItem(mensagem = mensagem)
                        }
                    }

                    // Área de interação baseada na seção atual
                    when (currentSection) {
                        ChatSection.MAIN_MENU -> {
                            MainMenuContent(
                                onPerguntasClick = { currentSection = ChatSection.PERGUNTAS_FREQUENTES },
                                onOracaoClick = {
                                    currentSection = ChatSection.PEDIDO_ORACAO
                                    mensagens.add(Mensagem("Por favor, compartilhe seu pedido de oração:", false))
                                },
                                onEnderecoClick = {
                                    currentSection = ChatSection.ATUALIZACAO_ENDERECO
                                    mensagens.add(Mensagem("Para atualizar seu endereço, precisamos das seguintes informações:", false))
                                    mensagens.add(Mensagem("Por favor, informe seu CEP:", false))
                                },
                                onContatoPastorClick = {
                                    currentSection = ChatSection.CONTATO_PASTOR
                                    mensagens.add(Mensagem("Você pode falar diretamente com o ${informacaoPastor.nome} através do WhatsApp ou ver os horários de atendimento.", false))
                                }
                            )

                            // Campo de entrada para perguntas gerais
                            OutlinedTextField(
                                value = mensagemInput,
                                onValueChange = { mensagemInput = it },
                                placeholder = { Text(if (iaRespondendo) "Aguarde a resposta..." else "Digite sua pergunta...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                trailingIcon = {
                                    if (iaRespondendo) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = PrimaryColor
                                        )
                                    } else {
                                        IconButton(
                                            onClick = {
                                                processarMensagem(mensagemInput)
                                            },
                                            enabled = mensagemInput.isNotBlank()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Enviar",
                                                tint = if (mensagemInput.isNotBlank()) PrimaryColor else Color.Gray
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (mensagemInput.isNotBlank() && !iaRespondendo) {
                                            processarMensagem(mensagemInput)
                                            keyboardController?.hide()
                                        }
                                    }
                                ),
                                enabled = !iaRespondendo
                            )
                        }

                        ChatSection.PERGUNTAS_FREQUENTES -> {
                            PerguntasFrequentesContent(
                                perguntas = perguntasFrequentes,
                                onPerguntaClick = { pergunta ->
                                    mensagens.add(Mensagem(pergunta.pergunta, true))
                                    mensagens.add(Mensagem(pergunta.resposta, false))
                                    currentSection = ChatSection.MAIN_MENU
                                },
                                onVoltar = { currentSection = ChatSection.MAIN_MENU }
                            )
                        }

                        ChatSection.PEDIDO_ORACAO -> {
                            PedidoOracaoContent(
                                pedido = oracaoPedido,
                                onPedidoChange = { oracaoPedido = it },
                                onSubmit = {
                                    keyboardController?.hide()
                                    enviarPedidoOracao()
                                },
                                onVoltar = { currentSection = ChatSection.MAIN_MENU }
                            )
                        }

                        ChatSection.ATUALIZACAO_ENDERECO -> {
                            AtualizacaoEnderecoContent(
                                cep = enderecoCep,
                                onCepChange = { enderecoCep = it },
                                rua = enderecoRua,
                                onRuaChange = { enderecoRua = it },
                                numero = enderecoNumero,
                                onNumeroChange = { enderecoNumero = it },
                                complemento = enderecoComplemento,
                                onComplementoChange = { enderecoComplemento = it },
                                bairro = enderecoBairro,
                                cidade = enderecoCidade,
                                estado = enderecoEstado,
                                onBuscarCep = { buscarEnderecoPorCep() },
                                buscandoCep = buscandoCep,
                                onSubmit = {
                                    keyboardController?.hide()
                                    validarEnviarAtualizacaoEndereco()
                                },
                                onVoltar = { currentSection = ChatSection.MAIN_MENU }
                            )
                        }

                        ChatSection.CONTATO_PASTOR -> {
                            ContatoPastorContent(
                                informacaoPastor = informacaoPastor,
                                onWhatsAppClick = { abrirWhatsAppPastor() },
                                onVoltar = { currentSection = ChatSection.MAIN_MENU }
                            )
                        }
                    }
                }
            }
        }

        // Botão flutuante de chat
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(60.dp)
                .shadow(6.dp, CircleShape),
            containerColor = PrimaryColor,
            contentColor = Color.White
        ) {
            // Ícone animado que muda entre chat e fechar
            val transition = updateTransition(isExpanded, label = "IconTransition")
            val rotation by transition.animateFloat(
                label = "IconRotation",
                transitionSpec = { tween(300) }
            ) { expanded -> if (expanded) 45f else 0f }

            Icon(
                imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Chat,
                contentDescription = if (isExpanded) "Fechar" else "Chat",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NotificacaoItem(notificacao: Notificacao) {
    val backgroundColor = when (notificacao.tipo) {
        TipoNotificacao.SUCESSO -> SuccessColor.copy(alpha = 0.9f)
        TipoNotificacao.ERRO -> ErrorColor.copy(alpha = 0.9f)
        TipoNotificacao.INFO -> PrimaryColor.copy(alpha = 0.9f)
        TipoNotificacao.AVISO -> AccentColor.copy(alpha = 0.9f)
    }

    val icon = when (notificacao.tipo) {
        TipoNotificacao.SUCESSO -> Icons.Filled.CheckCircle
        TipoNotificacao.ERRO -> Icons.Filled.Error
        TipoNotificacao.INFO -> Icons.Filled.Info
        TipoNotificacao.AVISO -> Icons.Filled.Warning
    }

    // Animação de entrada e saída
    val animatedAlpha = remember { Animatable(0f) }

    LaunchedEffect(notificacao.id) {
        animatedAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .alpha(animatedAlpha.value)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notificacao.titulo,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notificacao.mensagem,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MensagemItem(mensagem: Mensagem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (mensagem.isUsuario) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 260.dp),
            shape = RoundedCornerShape(
                topStart = if (mensagem.isUsuario) 16.dp else 4.dp,
                topEnd = if (mensagem.isUsuario) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (mensagem.isUsuario) PrimaryColor.copy(alpha = 0.9f) else Color(0xFFEEEEEE)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = mensagem.texto,
                color = if (mensagem.isUsuario) Color.White else TextPrimaryColor,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun MainMenuContent(
    onPerguntasClick: () -> Unit,
    onOracaoClick: () -> Unit,
    onEnderecoClick: () -> Unit,
    onContatoPastorClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ou escolha uma opção:",
            fontWeight = FontWeight.Medium,
            color = TextPrimaryColor,
            fontSize = 14.sp
        )

        // Primeira linha de opções
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuOptionChip(
                icon = Icons.Outlined.QuestionAnswer,
                text = "FAQs",
                onClick = onPerguntasClick,
                modifier = Modifier.weight(1f)
            )

            MenuOptionChip(
                icon = Icons.Outlined.Favorite,
                text = "Oração",
                onClick = onOracaoClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Segunda linha de opções
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuOptionChip(
                icon = Icons.Outlined.Home,
                text = "Endereço",
                onClick = onEnderecoClick,
                modifier = Modifier.weight(1f)
            )

            MenuOptionChip(
                icon = Icons.Outlined.Person,
                text = "Pastor",
                onClick = onContatoPastorClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MenuOptionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PrimaryColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = PrimaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PerguntasFrequentesContent(
    perguntas: List<PerguntaFrequente>,
    onPerguntaClick: (PerguntaFrequente) -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = PrimaryColor
                )
            }

            Text(
                text = "Perguntas Frequentes",
                fontWeight = FontWeight.Medium,
                color = TextPrimaryColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        perguntas.forEach { pergunta ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPerguntaClick(pergunta) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FF)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QuestionAnswer,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = pergunta.pergunta,
                        color = TextPrimaryColor,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoOracaoContent(
    pedido: String,
    onPedidoChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = PrimaryColor
                )
            }

            Text(
                text = "Pedido de Oração",
                fontWeight = FontWeight.Medium,
                color = TextPrimaryColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        OutlinedTextField(
            value = pedido,
            onValueChange = onPedidoChange,
            label = { Text("Seu pedido de oração") },
            placeholder = { Text("Descreva seu pedido de oração") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            )
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor
            ),
            enabled = pedido.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enviar Pedido de Oração",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtualizacaoEnderecoContent(
    cep: String,
    onCepChange: (String) -> Unit,
    rua: String,
    onRuaChange: (String) -> Unit,
    numero: String,
    onNumeroChange: (String) -> Unit,
    complemento: String,
    onComplementoChange: (String) -> Unit,
    bairro: String = "",
    cidade: String = "",
    estado: String = "",
    onBuscarCep: () -> Unit,
    buscandoCep: Boolean,
    onSubmit: () -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = PrimaryColor
                )
            }

            Text(
                text = "Atualização de Endereço",
                fontWeight = FontWeight.Medium,
                color = TextPrimaryColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // CEP com botão de busca
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = cep,
                onValueChange = onCepChange,
                label = { Text("CEP") },
                placeholder = { Text("00000-000") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuscarCep,
                enabled = cep.length >= 8 && !buscandoCep,
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                )
            ) {
                if (buscandoCep) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Buscar")
                }
            }
        }

        OutlinedTextField(
            value = rua,
            onValueChange = onRuaChange,
            label = { Text("Rua") },
            placeholder = { Text("Digite o nome da rua") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = numero,
            onValueChange = onNumeroChange,
            label = { Text("Número") },
            placeholder = { Text("Digite o número") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = complemento,
            onValueChange = onComplementoChange,
            label = { Text("Complemento (opcional)") },
            placeholder = { Text("Apto, bloco, etc.") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            )
        )

        // Informações adicionais do endereço (somente leitura)
        if (bairro.isNotEmpty() || cidade.isNotEmpty() || estado.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Informações do CEP",
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryColor,
                        fontSize = 14.sp
                    )

                    if (bairro.isNotEmpty()) {
                        Text(
                            text = "Bairro: $bairro",
                            color = TextSecondaryColor,
                            fontSize = 14.sp
                        )
                    }

                    if (cidade.isNotEmpty()) {
                        Text(
                            text = "Cidade: $cidade",
                            color = TextSecondaryColor,
                            fontSize = 14.sp
                        )
                    }

                    if (estado.isNotEmpty()) {
                        Text(
                            text = "Estado: $estado",
                            color = TextSecondaryColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor
            ),
            enabled = cep.isNotBlank() && rua.isNotBlank() && numero.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enviar Solicitação",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ContatoPastorContent(
    informacaoPastor: InformacaoPastor,
    onWhatsAppClick: () -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = PrimaryColor
                )
            }

            Text(
                text = "Contato com o Pastor",
                fontWeight = FontWeight.Medium,
                color = TextPrimaryColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Informações do pastor
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F7FF)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = informacaoPastor.nome,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryColor,
                    fontSize = 18.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Horário de atendimento: ${informacaoPastor.horarioAtendimento}",
                        color = TextSecondaryColor,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Para assuntos urgentes ou aconselhamento pastoral, entre em contato diretamente pelo WhatsApp:",
            color = TextPrimaryColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onWhatsAppClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366) // Cor do WhatsApp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Conversar pelo WhatsApp",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Exemplo de uso do componente
@Composable
fun TelaComChatIgreja() {
    // Usuário logado (simulado)
    val usuarioLogado = remember {
        UsuarioLogado(
            id = "123456",
            nome = "Maria Silva",
            email = "maria@email.com",
            telefone = "5511999887766"
        )
    }

    // Informações do pastor
    val informacaoPastor = remember {
        InformacaoPastor(
            nome = "Pastor João Silva",
            telefone = "5511999999999",
            horarioAtendimento = "Segunda a Sexta, 9h às 17h"
        )
    }

    // Seu conteúdo de tela aqui
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Conteúdo principal da tela

        // Adicione o chat da igreja com callbacks para processar os pedidos
        IgrejaChatComponent(
            nomeIgreja = "Igreja Batista Vila Maria",
            usuarioLogado = usuarioLogado,
            informacaoPastor = informacaoPastor,
            onPedidoOracaoEnviado = { pedidoOracao ->
                // Aqui você implementa a lógica para enviar o pedido de oração
                // para o administrador em outra parte do app
                println("Pedido recebido: ${pedidoOracao.nome} - ${pedidoOracao.pedido}")
            },
            onAtualizacaoEnderecoEnviada = { atualizacao ->
                // Aqui você implementa a lógica para enviar a atualização de endereço
                // para o administrador em outra parte do app
                println("Atualização recebida: ${atualizacao.nome} - ${atualizacao.rua}, ${atualizacao.numero}")
            },
            onBuscarEnderecoPorCep = { cep ->
                // Implementação da busca de CEP (usando ViaCEP ou outra API)
                try {
                    val url = URL("https://viacep.com.br/ws/$cep/json/")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)

                    if (!jsonObject.has("erro")) {
                        mapOf(
                            "logradouro" to jsonObject.optString("logradouro", ""),
                            "bairro" to jsonObject.optString("bairro", ""),
                            "localidade" to jsonObject.optString("localidade", ""),
                            "uf" to jsonObject.optString("uf", "")
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun MyEventsPreview() {
    TelaComChatIgreja()
}