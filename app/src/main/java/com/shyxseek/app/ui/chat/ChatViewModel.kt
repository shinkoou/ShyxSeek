package com.shyxseek.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.agent.AgentOrchestrator
import com.shyxseek.app.data.local.MessageEntity
import com.shyxseek.app.data.repository.ConversationRepository
import com.shyxseek.app.data.repository.MemoryRepository
import com.shyxseek.app.domain.ChatMessage
import com.shyxseek.app.domain.MemoryType
import com.shyxseek.app.domain.MessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val conversationId: Long? = null,
    val messages: List<MessageEntity> = emptyList(),
    val draft: String = "",
    val generating: Boolean = false,
    val error: String? = null
)

private data class LearningCommand(
    val content: String,
    val type: MemoryType,
    val confirmation: String
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ConversationRepository,
    private val agent: AgentOrchestrator,
    private val memoryRepo: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var observe: Job? = null
    private var generation: Job? = null

    init {
        viewModelScope.launch {
            open(repo.create())
        }
    }

    fun draft(value: String) {
        _state.update { it.copy(draft = value) }
    }

    fun useLearningShortcut(prefix: String) {
        val current = _state.value.draft
        if (current.isBlank()) {
            _state.update { it.copy(draft = "$prefix ") }
        } else if (!current.startsWith(prefix, ignoreCase = true)) {
            _state.update { it.copy(draft = "$prefix $current") }
        }
    }

    private fun open(id: Long) {
        observe?.cancel()
        _state.update { it.copy(conversationId = id) }
        observe = viewModelScope.launch {
            repo.messages(id).collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    fun send() {
        val text = state.value.draft.trim()
        val id = state.value.conversationId ?: return
        if (text.isBlank() || state.value.generating) return

        _state.update {
            it.copy(draft = "", generating = true, error = null)
        }

        generation = viewModelScope.launch {
            try {
                repo.add(id, MessageRole.USER, text)

                val learning = parseLearningCommand(text)
                if (learning != null) {
                    memoryRepo.remember(
                        content = learning.content,
                        type = learning.type
                    )
                    repo.add(
                        id,
                        MessageRole.ASSISTANT,
                        learning.confirmation
                    )
                    return@launch
                }

                val history = repo.list(id).map {
                    ChatMessage(it.role, it.content)
                }

                val answer = StringBuilder()
                agent.stream(history, null, null).collect { chunk ->
                    if (chunk.text.isNotEmpty()) answer.append(chunk.text)
                }

                repo.add(
                    id,
                    MessageRole.ASSISTANT,
                    answer.toString().ifBlank {
                        "Não recebi conteúdo do provider. Tente novamente."
                    }
                )
            } catch (t: Throwable) {
                _state.update {
                    it.copy(error = t.message ?: "Falha desconhecida")
                }
            } finally {
                _state.update { it.copy(generating = false) }
            }
        }
    }

    fun stop() {
        generation?.cancel()
        _state.update { it.copy(generating = false) }
    }

    private fun parseLearningCommand(message: String): LearningCommand? {
        val trimmed = message.trim()
        val normalized = trimmed.lowercase()

        val commands = listOf(
            Triple("ensine que", MemoryType.KNOWLEDGE, "Aprendi"),
            Triple("lembre que", MemoryType.LONG_TERM, "Vou lembrar"),
            Triple("guarde que", MemoryType.LONG_TERM, "Guardei")
        )

        for ((prefix, type, verb) in commands) {
            if (normalized == prefix || normalized.startsWith("$prefix ")) {
                val content = trimmed
                    .drop(prefix.length)
                    .trim()
                    .trimStart(':', '-', ' ')

                if (content.isBlank()) return null

                val confirmation = buildString {
                    append("$verb e salvei localmente:\n\n")
                    append("“$content”\n\n")
                    append("Essa informação fica neste dispositivo e poderá ser usada como contexto nas próximas conversas.")
                }

                return LearningCommand(
                    content = content,
                    type = type,
                    confirmation = confirmation
                )
            }
        }

        return null
    }
}
