package com.shyxseek.app.ui

import androidx.compose.foundation.BorderStroke
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
            Surface(
                modifier = Modifier.size(92.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF23104D),
                border = BorderStroke(1.dp, Color(0xFF4F27A8))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 52.sp
                    )
                }
            }

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
                icon = if (state.provider == "fake") Icons.Default.CloudOff else Icons.Default.CheckCircle,
                text = if (state.provider == "fake") "Offline de teste" else "OpenAI ativa",
                accent = if (state.provider == "fake") ShyxPurpleSoft else Color(0xFF77D6A3)
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
private fun AppHeader(
    title: String,
    subtitle: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF25124F),
            border = BorderStroke(1.dp, Color(0xFF45228C))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "S",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
        }

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
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = Color(0xFF281252)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "S",
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        project.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        project.status,
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

    var provider by remember(state.provider) {
        mutableStateOf(state.provider)
    }
    var model by remember(state.model) {
        mutableStateOf(state.model.ifBlank { "gpt-5.6-luna" })
    }
    var apiKey by remember { mutableStateOf("") }

    val models = remember {
        listOf(
            "gpt-5.6-luna" to "Luna · econômico",
            "gpt-5.6-terra" to "Terra · equilibrado",
            "gpt-5.6-sol" to "Sol · máximo"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        AppHeader("Ajustes", "Configure como o ShyxSeek responde")
        Spacer(Modifier.height(16.dp))

        SettingsSection("Escolha a inteligência") {
            Text(
                text = "Sem Base URL e sem configuração técnica.",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))

            FilterChip(
                selected = provider == "fake",
                onClick = {
                    provider = "fake"
                    vm.clearConnectionMessage()
                },
                label = { Text("Offline de teste") },
                leadingIcon = {
                    Icon(Icons.Default.CloudOff, contentDescription = null)
                }
            )

            Spacer(Modifier.height(8.dp))

            FilterChip(
                selected = provider == "openai_compatible",
                onClick = {
                    provider = "openai_compatible"
                    vm.clearConnectionMessage()
                },
                label = { Text("OpenAI") },
                leadingIcon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                }
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (provider == "fake") {
                    "Serve para testar o app sem conta e sem gastar API."
                } else {
                    "Usa IA real da OpenAI. A API é separada da assinatura do ChatGPT."
                },
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (provider == "openai_compatible") {
            Spacer(Modifier.height(12.dp))

            SettingsSection("Modelo") {
                Text(
                    text = "Para começar, Luna é o perfil mais econômico.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(10.dp))

                models.forEach { (id, label) ->
                    FilterChip(
                        selected = model == id,
                        onClick = {
                            model = id
                            vm.clearConnectionMessage()
                        },
                        label = { Text(label) }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            SettingsSection("Conectar OpenAI") {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        vm.clearConnectionMessage()
                    },
                    label = {
                        Text(
                            if (state.hasApiKey) {
                                "API key (já existe uma salva)"
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

                Text(
                    text = "Sua chave fica salva localmente pelo Android Keystore.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { vm.testOpenAI(model, apiKey) },
                    enabled = !connection.testing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (connection.testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Testando…")
                    } else {
                        Text("Testar conexão")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { vm.saveOpenAI(model, apiKey) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar OpenAI no chat")
                }

                connection.message?.let { message ->
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = when (connection.success) {
                            true -> Color(0xFF10271C)
                            false -> Color(0xFF351419)
                            null -> SurfaceDark2
                        },
                        border = BorderStroke(
                            1.dp,
                            when (connection.success) {
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
                            color = when (connection.success) {
                                true -> Color(0xFF8DE6B5)
                                false -> Color(0xFFFFA7AE)
                                null -> MutedText
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.height(12.dp))

            SettingsSection("Modo offline") {
                Text(
                    text = "Continua disponível para testar chat, memória e interface sem API.",
                    color = MutedText
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { vm.useOffline() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar modo offline")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        SettingsSection("Memória e privacidade") {
            InfoRow(Icons.Default.Lock, "Memórias ficam salvas localmente")
            InfoRow(Icons.Default.CheckCircle, "Sem analytics e sem anúncios")
            InfoRow(Icons.Default.CheckCircle, "Sem upload automático")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Aprendizado acontece pelo chat com “Ensine que”, “Lembre que” e “Guarde que”.",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))
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
