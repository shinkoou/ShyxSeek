package com.shyxseek.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shyxseek.app.R
import com.shyxseek.app.data.local.MemoryEntity
import com.shyxseek.app.data.local.ProjectEntity
import com.shyxseek.app.domain.MemoryType
import com.shyxseek.app.domain.MessageRole
import com.shyxseek.app.ui.chat.ChatViewModel
import com.shyxseek.app.ui.screens.MemoryViewModel
import com.shyxseek.app.ui.screens.ProjectsViewModel
import com.shyxseek.app.ui.screens.SettingsViewModel
import kotlinx.coroutines.delay

private val ShyxPurple = Color(0xFF6C3BFF)
private val ShyxPurpleSoft = Color(0xFF9B7BFF)
private val SurfaceDark = Color(0xFF121216)
private val SurfaceDark2 = Color(0xFF19191F)
private val BorderDark = Color(0xFF292932)
private val MutedText = Color(0xFFAAAAAF)

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun ShyxSeekApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(650)
        showSplash = false
    }

    if (showSplash) {
        ShyxSeekSplash()
    } else {
        ShyxSeekMain()
    }
}

@Composable
private fun ShyxSeekSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandIcon(
                modifier = Modifier.size(104.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "ShyxSeek",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Assistente pessoal de IA",
                color = MutedText
            )
        }

        Text(
            text = "Preparando contexto local…",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            color = ShyxPurpleSoft,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ShyxSeekMain() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "chat"

    val items = remember {
        listOf(
            NavItem("chat", "Chat", Icons.Default.Chat),
            NavItem("memory", "Memória", Icons.Default.Memory),
            NavItem("projects", "Projetos", Icons.Default.Folder),
            NavItem("settings", "Ajustes", Icons.Default.Settings)
        )
    }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF101014),
                tonalElevation = 0.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                nav.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ShyxPurpleSoft,
                            selectedTextColor = ShyxPurpleSoft,
                            indicatorColor = Color(0xFF241541),
                            unselectedIconColor = MutedText,
                            unselectedTextColor = MutedText
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "chat",
            modifier = Modifier.padding(padding)
        ) {
            composable("chat") { ChatScreen() }
            composable("memory") { MemoryScreen() }
            composable("projects") { ProjectsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun ChatScreen(vm: ChatViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        AppHeader(
            title = "ShyxSeek",
            subtitle = "Seu assistente pessoal"
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPill(
                icon = when (state.provider) {
                    "local_litert" -> Icons.Default.Memory
                    "gemini_free" -> Icons.Default.Language
                    "openai_compatible" -> Icons.Default.CheckCircle
                    else -> Icons.Default.CloudOff
                },
                text = when (state.provider) {
                    "local_litert" -> "IA local"
                    "gemini_free" -> "Gemini grátis"
                    "openai_compatible" -> "OpenAI"
                    else -> "Offline de teste"
                },
                accent = if (state.provider == "fake") {
                    ShyxPurpleSoft
                } else {
                    Color(0xFF77D6A3)
                }
            )
            StatusPill(
                icon = Icons.Default.Lock,
                text = "Memória local",
                accent = Color(0xFF77D6A3)
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    WelcomeCard()
                }
            }

            items(state.messages, key = { it.id }) { message ->
                MessageBubble(
                    isUser = message.role == MessageRole.USER,
                    content = message.content
                )
            }

            if (state.generating) {
                item {
                    Text(
                        text = "ShyxSeek está respondendo…",
                        color = ShyxPurpleSoft,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        state.error?.let {
            Surface(
                color = Color(0xFF351419),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = it,
                    color = Color(0xFFFFA7AE),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LearningChip("Ensine que", Icons.Default.School) {
                vm.useLearningShortcut("Ensine que")
            }
            LearningChip("Lembre que", Icons.Default.Bookmark) {
                vm.useLearningShortcut("Lembre que")
            }
            LearningChip("Guarde que", Icons.Default.Save) {
                vm.useLearningShortcut("Guarde que")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = vm::draft,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mensagem para o ShyxSeek…") },
                maxLines = 5,
                shape = RoundedCornerShape(22.dp)
            )

            FilledIconButton(
                onClick = {
                    if (state.generating) vm.stop() else vm.send()
                },
                enabled = state.generating || state.draft.isNotBlank(),
                modifier = Modifier.size(54.dp)
            ) {
                Icon(
                    imageVector = if (state.generating) Icons.Default.Stop else Icons.Default.Send,
                    contentDescription = if (state.generating) "Parar" else "Enviar"
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = "Olá. Eu sou o ShyxSeek.",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Converse normalmente ou me ensine algo usando “Ensine que”, “Lembre que” ou “Guarde que”.",
                color = MutedText
            )
        }
    }
}

@Composable
private fun MessageBubble(
    isUser: Boolean,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 560.dp),
            color = if (isUser) Color(0xFF38206E) else SurfaceDark2,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 20.dp
            ),
            border = if (isUser) null else BorderStroke(1.dp, BorderDark)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = if (isUser) "Você" else "ShyxSeek",
                    color = ShyxPurpleSoft,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = content,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun LearningChip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = SurfaceDark,
            labelColor = Color.White,
            leadingIconContentColor = ShyxPurpleSoft
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = BorderDark
        )
    )
}

@Composable
private fun StatusPill(
    icon: ImageVector,
    text: String,
    accent: Color
) {
    Surface(
        shape = CircleShape,
        color = SurfaceDark,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
            Text(
                text = text,
                color = MutedText,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun BrandIcon(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.shyxseek_brand),
        contentDescription = "ShyxSeek",
        modifier = modifier
    )
}

@Composable
private fun AppHeader(
    title: String,
    subtitle: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandIcon(
            modifier = Modifier.size(46.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MemoryScreen(vm: MemoryViewModel = hiltViewModel()) {
    val memories by vm.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        AppHeader("Memória", "Tudo que você pediu para guardar")
        Spacer(Modifier.height(14.dp))

        Surface(
            color = Color(0xFF171126),
            border = BorderStroke(1.dp, Color(0xFF34235B)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ShyxPurpleSoft
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Aprendizado via chat",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Use “Ensine que”, “Lembre que” ou “Guarde que”.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (memories.isEmpty()) {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nenhuma memória salva ainda. Volte ao chat e ensine algo ao ShyxSeek.",
                    color = MutedText,
                    modifier = Modifier.padding(18.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories, key = { it.id }) { memory ->
                    MemoryCard(memory)
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(memory: MemoryEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = memoryTypeLabel(memory.memoryType),
                color = ShyxPurpleSoft,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = memory.content,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Salvo localmente",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun memoryTypeLabel(type: MemoryType): String = when (type) {
    MemoryType.KNOWLEDGE -> "Conhecimento"
    MemoryType.PREFERENCE -> "Preferência"
    MemoryType.PROJECT -> "Projeto"
    MemoryType.LONG_TERM -> "Longo prazo"
    MemoryType.CONVERSATION -> "Conversa"
    MemoryType.TEMPORARY -> "Temporária"
    MemoryType.SENSITIVE -> "Sensível"
}

@Composable
fun ProjectsScreen(vm: ProjectsViewModel = hiltViewModel()) {
    val projects by vm.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        AppHeader("Projetos", "Contextos de trabalho do ShyxSeek")
        Spacer(Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects, key = { it.id }) { project ->
                ProjectCard(project)
            }
        }
    }
}

@Composable
private fun ProjectCard(project: ProjectEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15131B)),
        border = BorderStroke(1.dp, Color(0xFF302640)),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandIcon(
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        project.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (project.status == "initial development") "Desenvolvimento inicial" else project.status,
                        color = ShyxPurpleSoft
                    )
                }
            }

            if (project.goal.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = BorderDark)
                Spacer(Modifier.height(14.dp))
                Text(
                    "Objetivo",
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    project.goal,
                    color = Color.White
                )
            }

            if (project.currentState.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Estado atual",
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    project.currentState,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val connection by vm.connection.collectAsState()
    val localState by vm.localState.collectAsState()

    var provider by remember(state.provider) {
        mutableStateOf(state.provider)
    }
    var openAiModel by remember(state.openAiModel) {
        mutableStateOf(state.openAiModel)
    }
    var geminiModel by remember(state.geminiModel) {
        mutableStateOf(state.geminiModel)
    }
    var localBackend by remember(state.localBackend) {
        mutableStateOf(state.localBackend)
    }
    var openAiKey by remember { mutableStateOf("") }
    var geminiKey by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(vm::importLocalModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        AppHeader("Ajustes", "IA local, gratuita e na nuvem")
        Spacer(Modifier.height(16.dp))

        SettingsSection("Inteligência") {
            Text(
                text = "Escolha como o ShyxSeek vai responder. Você pode trocar quando quiser.",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))

            ProviderChoiceCard(
                title = "IA local",
                subtitle = "Roda no celular · sem API · sem cobrança por mensagem",
                selected = provider == "local_litert",
                icon = Icons.Default.Memory,
                onClick = {
                    provider = "local_litert"
                    vm.clearConnectionMessage()
                }
            )

            Spacer(Modifier.height(8.dp))

            ProviderChoiceCard(
                title = "Gemini grátis",
                subtitle = "IA online com cota gratuita · requer chave do Google AI Studio",
                selected = provider == "gemini_free",
                icon = Icons.Default.Language,
                onClick = {
                    provider = "gemini_free"
                    vm.clearConnectionMessage()
                }
            )

            Spacer(Modifier.height(8.dp))

            ProviderChoiceCard(
                title = "OpenAI",
                subtitle = "IA real pela API da OpenAI · cobrança por uso",
                selected = provider == "openai_compatible",
                icon = Icons.Default.CheckCircle,
                onClick = {
                    provider = "openai_compatible"
                    vm.clearConnectionMessage()
                }
            )

            Spacer(Modifier.height(8.dp))

            ProviderChoiceCard(
                title = "Offline de teste",
                subtitle = "Sem conta e sem internet · respostas demonstrativas",
                selected = provider == "fake",
                icon = Icons.Default.CloudOff,
                onClick = {
                    provider = "fake"
                    vm.clearConnectionMessage()
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        when (provider) {
            "local_litert" -> {
                SettingsSection("Modelo local") {
                    Surface(
                        color = if (localState.installed) {
                            Color(0xFF10271C)
                        } else {
                            SurfaceDark2
                        },
                        border = BorderStroke(
                            1.dp,
                            if (localState.installed) {
                                Color(0xFF275C41)
                            } else {
                                BorderDark
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                text = if (localState.installed) {
                                    "Qwen3 0.6B INT4 instalado"
                                } else {
                                    "Qwen3 0.6B INT4 recomendado"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (localState.installed) {
                                    "Pronto para conversar sem enviar mensagens para a nuvem."
                                } else {
                                    "Aproximadamente 350 MB. Otimizado para respostas diretas no aparelho."
                                },
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (localState.installed && localState.sizeBytes > 0L) {
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    text = "Arquivo: ${formatModelSize(localState.sizeBytes)}",
                                    color = Color(0xFF8DE6B5),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (localState.busy) {
                        Spacer(Modifier.height(12.dp))
                        if (localState.progress >= 0) {
                            LinearProgressIndicator(
                                progress = { localState.progress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "${localState.progress}% concluído",
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    localState.message?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        ConnectionMessage(
                            success = localState.installed,
                            message = message
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    if (!localState.installed) {
                        Button(
                            onClick = vm::downloadLocalModel,
                            enabled = !localState.busy,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Baixar modelo recomendado")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(
                                arrayOf(
                                    "application/octet-stream",
                                    "application/x-binary",
                                    "*/*"
                                )
                            )
                        },
                        enabled = !localState.busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (localState.installed) {
                                "Substituir por arquivo .litertlm"
                            } else {
                                "Importar arquivo .litertlm"
                            }
                        )
                    }

                    if (localState.installed) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Desempenho",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        ModelChoiceCard(
                            title = "CPU · estável",
                            subtitle = "Maior compatibilidade. Recomendado para começar.",
                            selected = localBackend == "cpu",
                            onClick = {
                                localBackend = "cpu"
                                vm.clearConnectionMessage()
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        ModelChoiceCard(
                            title = "GPU · rápido",
                            subtitle = "Usa OpenCL quando disponível. Se der erro, volte para CPU.",
                            selected = localBackend == "gpu",
                            onClick = {
                                localBackend = "gpu"
                                vm.clearConnectionMessage()
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { vm.useLocal(localBackend) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Usar IA local no chat")
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = vm::deleteLocalModel,
                            enabled = !localState.busy,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Remover modelo local")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    InfoRow(
                        Icons.Default.Lock,
                        "Suas mensagens ficam no aparelho quando a IA local está ativa"
                    )
                }
            }

            "gemini_free" -> {
                SettingsSection("Gemini gratuito") {
                    Text(
                        text = "Escolha um modelo com cota gratuita.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))

                    ModelChoiceCard(
                        title = "Gemini 3.1 Flash-Lite",
                        subtitle = "Mais econômico · recomendado para uso diário",
                        selected = geminiModel == "gemini-3.1-flash-lite",
                        onClick = {
                            geminiModel = "gemini-3.1-flash-lite"
                            vm.clearConnectionMessage()
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    ModelChoiceCard(
                        title = "Gemini 3.6 Flash",
                        subtitle = "Mais forte · também possui nível gratuito",
                        selected = geminiModel == "gemini-3.6-flash",
                        onClick = {
                            geminiModel = "gemini-3.6-flash"
                            vm.clearConnectionMessage()
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = {
                            geminiKey = it
                            vm.clearConnectionMessage()
                        },
                        label = {
                            Text(
                                if (state.hasGeminiKey) {
                                    "Chave Gemini · já existe uma salva"
                                } else {
                                    "Chave Gemini"
                                }
                            )
                        },
                        placeholder = { Text("Cole a chave do Google AI Studio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(8.dp))
                    InfoRow(
                        Icons.Default.Lock,
                        "A chave fica protegida pelo Android Keystore"
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { vm.testGemini(geminiModel, geminiKey) },
                        enabled = !connection.testing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (connection.testing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Testando conexão…")
                        } else {
                            Text("Testar Gemini")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { vm.saveGemini(geminiModel, geminiKey) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar Gemini no chat")
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "O nível gratuito tem limites de uso. No nível gratuito, o Google informa que o conteúdo pode ser usado para melhorar produtos.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )

                    connection.message?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        ConnectionMessage(
                            success = connection.success,
                            message = message
                        )
                    }
                }
            }

            "openai_compatible" -> {
                SettingsSection("OpenAI") {
                    ModelChoiceCard(
                        title = "Luna",
                        subtitle = "Econômico · ideal para uso diário",
                        selected = openAiModel == "gpt-5.6-luna",
                        onClick = {
                            openAiModel = "gpt-5.6-luna"
                            vm.clearConnectionMessage()
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    ModelChoiceCard(
                        title = "Terra",
                        subtitle = "Equilíbrio entre inteligência e custo",
                        selected = openAiModel == "gpt-5.6-terra",
                        onClick = {
                            openAiModel = "gpt-5.6-terra"
                            vm.clearConnectionMessage()
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    ModelChoiceCard(
                        title = "Sol",
                        subtitle = "Máxima capacidade para tarefas complexas",
                        selected = openAiModel == "gpt-5.6-sol",
                        onClick = {
                            openAiModel = "gpt-5.6-sol"
                            vm.clearConnectionMessage()
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = openAiKey,
                        onValueChange = {
                            openAiKey = it
                            vm.clearConnectionMessage()
                        },
                        label = {
                            Text(
                                if (state.hasOpenAiKey) {
                                    "API key · já existe uma salva"
                                } else {
                                    "API key"
                                }
                            )
                        },
                        placeholder = { Text("sk-…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(8.dp))
                    InfoRow(
                        Icons.Default.Lock,
                        "Armazenada localmente pelo Android Keystore"
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { vm.testOpenAI(openAiModel, openAiKey) },
                        enabled = !connection.testing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (connection.testing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Testando conexão…")
                        } else {
                            Text("Testar OpenAI")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { vm.saveOpenAI(openAiModel, openAiKey) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar OpenAI no chat")
                    }

                    connection.message?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        ConnectionMessage(
                            success = connection.success,
                            message = message
                        )
                    }
                }
            }

            else -> {
                SettingsSection("Modo offline") {
                    Text(
                        text = "Mantém chat, memória e interface disponíveis para teste sem usar um modelo de IA real.",
                        color = MutedText
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = vm::useOffline,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar modo offline de teste")
                    }

                    connection.message?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        ConnectionMessage(
                            success = connection.success,
                            message = message
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        SettingsSection("Memória e privacidade") {
            InfoRow(Icons.Default.Lock, "Memórias do ShyxSeek salvas localmente")
            InfoRow(Icons.Default.CheckCircle, "Sem analytics e sem anúncios")
            InfoRow(Icons.Default.CheckCircle, "Gemini usa store=false no ShyxSeek")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ensine pelo chat usando “Ensine que”, “Lembre que” ou “Guarde que”. O ShyxSeek recupera essas informações quando forem relevantes.",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(12.dp))

        SettingsSection("Sobre") {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandIcon(modifier = Modifier.size(38.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ShyxSeek",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "v0.4.0 · IA local + Gemini gratuito",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun formatModelSize(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return String.format("%.0f MB", mb)
}

@Composable
private fun ProviderChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Color(0xFF21173A) else SurfaceDark2,
        border = BorderStroke(
            1.dp,
            if (selected) ShyxPurple else BorderDark
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (selected) Color(0xFF39216A) else Color(0xFF202027),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (selected) ShyxPurpleSoft else MutedText,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = ShyxPurpleSoft
                )
            }
        }
    }
}

@Composable
private fun ModelChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Color(0xFF21173A) else SurfaceDark2,
        border = BorderStroke(
            1.dp,
            if (selected) ShyxPurple else BorderDark
        ),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = ShyxPurpleSoft
                )
            }
        }
    }
}

@Composable
private fun ConnectionMessage(
    success: Boolean?,
    message: String
) {
    Surface(
        color = when (success) {
            true -> Color(0xFF10271C)
            false -> Color(0xFF351419)
            null -> SurfaceDark2
        },
        border = BorderStroke(
            1.dp,
            when (success) {
                true -> Color(0xFF275C41)
                false -> Color(0xFF6A2930)
                null -> BorderDark
            }
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = when (success) {
                true -> Color(0xFF8DE6B5)
                false -> Color(0xFFFFA7AE)
                null -> MutedText
            },
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = SurfaceDark,
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = ShyxPurpleSoft,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
