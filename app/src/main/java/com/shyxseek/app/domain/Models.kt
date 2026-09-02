package com.shyxseek.app.domain

import kotlinx.serialization.Serializable

@Serializable enum class MessageRole { USER, ASSISTANT, TOOL, SYSTEM }
@Serializable enum class MemoryType { TEMPORARY, CONVERSATION, PROJECT, KNOWLEDGE, PREFERENCE, LONG_TERM, SENSITIVE }
@Serializable enum class KnowledgeSource { USER_TAUGHT, PROJECT, FILE, WEB, AI_GENERATED, TRANSFERRED_CONTEXT }
@Serializable enum class PermissionLevel { SAFE_READ, EXTERNAL_READ, LOCAL_WRITE, EXTERNAL_WRITE, DESTRUCTIVE }
@Serializable enum class Capability { TEXT_GENERATION, VISION, WEB_SEARCH, FILE_ANALYSIS, IMAGE_GENERATION, IMAGE_EDITING, CODE_EXECUTION, VOICE, TOOLS, AUTOMATIONS, STREAMING, EMBEDDINGS }

@Serializable
data class ChatMessage(val role: MessageRole, val content: String)

@Serializable
data class ChatRequest(
    val messages: List<ChatMessage>,
    val systemPrompt: String,
    val modelId: String,
    val temperature: Double = 0.4,
    val maxOutputTokens: Int = 2048
)

data class ChatChunk(val text: String, val done: Boolean = false)

data class ProviderCapabilities(
    val capabilities: Set<Capability>
) { fun has(capability: Capability) = capability in capabilities }
