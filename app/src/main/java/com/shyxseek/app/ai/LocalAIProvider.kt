package com.shyxseek.app.ai

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import com.shyxseek.app.domain.Capability
import com.shyxseek.app.domain.ChatChunk
import com.shyxseek.app.domain.ChatRequest
import com.shyxseek.app.domain.MessageRole
import com.shyxseek.app.domain.ProviderCapabilities
import com.shyxseek.app.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.first
import java.io.File

class LocalAIProvider(
    private val models: LocalModelManager,
    private val settings: AppSettings
) : AIProvider {

    override val id = "local_litert"
    override val displayName = "IA local"

    override val capabilities = ProviderCapabilities(
        setOf(
            Capability.TEXT_GENERATION,
            Capability.STREAMING
        )
    )

    @Volatile
    private var engine: Engine? = null

    @Volatile
    private var engineSignature: String? = null

    override fun streamChat(request: ChatRequest): Flow<ChatChunk> = flow {
        val path = models.modelPath()
            ?: error(
                "O modelo local ainda não está instalado. " +
                    "Abra Ajustes > IA local e baixe ou importe um modelo."
            )

        val config = settings.flow.first()
        val localEngine = ensureEngine(path, config.localBackend)

        val currentPrompt = request.messages.lastOrNull()?.content.orEmpty()
        if (currentPrompt.isBlank()) {
            error("Mensagem vazia.")
        }

        val initial = request.messages
            .dropLast(1)
            .takeLast(12)
            .mapNotNull { message ->
                when (message.role) {
                    MessageRole.USER -> Message.user(message.content)
                    MessageRole.ASSISTANT -> Message.model(message.content)
                    MessageRole.TOOL -> Message.user(
                        "[Resultado de ferramenta]\n${message.content}"
                    )
                    MessageRole.SYSTEM -> null
                }
            }

        val conversation = localEngine.createConversation(
            ConversationConfig(
                systemInstruction = Contents.of(request.systemPrompt),
                initialMessages = initial,
                samplerConfig = SamplerConfig(
                    topK = 40,
                    topP = 0.9,
                    temperature = request.temperature
                ),
                maxOutputToken = request.maxOutputTokens
            )
        )

        try {
            conversation
                .sendMessageAsync(
                    currentPrompt,
                    maxOutputToken = request.maxOutputTokens
                )
                .collect { message ->
                    val text = message.toString()
                    if (text.isNotEmpty()) {
                        emit(ChatChunk(text))
                    }
                }

            emit(ChatChunk("", true))
        } finally {
            runCatching { conversation.close() }
        }
    }.flowOn(Dispatchers.IO)

    @Synchronized
    private fun ensureEngine(
        path: String,
        backendName: String
    ): Engine {
        val file = File(path)
        val signature =
            "${file.absolutePath}:${file.length()}:${file.lastModified()}:$backendName"

        engine?.let { existing ->
            if (engineSignature == signature && existing.isInitialized()) {
                return existing
            }
        }

        reset()

        val backend = if (backendName == "gpu") {
            Backend.GPU()
        } else {
            Backend.CPU(threadCount = 4)
        }

        return Engine(
            EngineConfig(
                modelPath = file.absolutePath,
                backend = backend,
                cacheDir = file.parentFile?.absolutePath
            )
        ).also { created ->
            created.initialize()
            engine = created
            engineSignature = signature
        }
    }

    @Synchronized
    fun reset() {
        engine?.let { current ->
            runCatching {
                if (current.isInitialized()) current.close()
            }
        }
        engine = null
        engineSignature = null
    }
}
