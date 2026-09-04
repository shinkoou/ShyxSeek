package com.shyxseek.app.ai

import com.shyxseek.app.domain.Capability
import com.shyxseek.app.domain.ChatChunk
import com.shyxseek.app.domain.ChatRequest
import com.shyxseek.app.domain.MessageRole
import com.shyxseek.app.domain.ProviderCapabilities
import com.shyxseek.app.settings.AppSettings
import com.shyxseek.app.settings.GEMINI_BASE_URL
import com.shyxseek.app.settings.GEMINI_DEFAULT_MODEL
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class GeminiProvider(
    private val client: OkHttpClient,
    private val settings: AppSettings
) : AIProvider {

    override val id = "gemini_free"
    override val displayName = "Gemini grátis"

    override val capabilities = ProviderCapabilities(
        setOf(
            Capability.TEXT_GENERATION,
            Capability.STREAMING
        )
    )

    override fun streamChat(request: ChatRequest): Flow<ChatChunk> = callbackFlow {
        val current = settings.flow.first()
        val key = settings.geminiKey()

        if (key.isNullOrBlank()) {
            close(
                IllegalStateException(
                    "O Gemini ainda não está conectado. " +
                        "Abra Ajustes e adicione sua chave gratuita do Google AI Studio."
                )
            )
            return@callbackFlow
        }

        val model = current.model.ifBlank { GEMINI_DEFAULT_MODEL }

        val transcript = request.messages
            .takeLast(20)
            .joinToString(separator = "\n\n") { message ->
                val role = when (message.role) {
                    MessageRole.USER -> "Usuário"
                    MessageRole.ASSISTANT -> "ShyxSeek"
                    MessageRole.SYSTEM -> "Sistema"
                    MessageRole.TOOL -> "Ferramenta"
                }
                "$role: ${message.content}"
            }

        val payload = buildJsonObject {
            put("model", model)
            put("system_instruction", request.systemPrompt)
            put("input", transcript)
            put("stream", true)
            put("store", false)
            putJsonObject("generation_config") {
                put("temperature", current.temperature)
                put("max_output_tokens", request.maxOutputTokens)
            }
        }

        val body = payload
            .toString()
            .toRequestBody("application/json".toMediaType())

        val call = client.newCall(
            Request.Builder()
                .url("$GEMINI_BASE_URL/v1beta/interactions?alt=sse")
                .header("x-goog-api-key", key)
                .header("Accept", "text/event-stream")
                .post(body)
                .build()
        )

        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    close(IOException("Falha ao conectar ao Gemini: ${e.message}", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        val detail = response.body.string().take(600)
                        val message = friendlyError(response.code, detail)
                        response.close()
                        close(IOException(message))
                        return
                    }

                    try {
                        val source = response.body.source()

                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue

                            val data = line.removePrefix("data:").trim()
                            if (data.isBlank()) continue

                            if (data == "[DONE]") {
                                trySend(ChatChunk("", true))
                                break
                            }

                            val event = runCatching {
                                Json.parseToJsonElement(data).jsonObject
                            }.getOrNull() ?: continue

                            when (event["event_type"]?.jsonPrimitive?.contentOrNull) {
                                "step.delta" -> {
                                    val delta = event["delta"]?.jsonObject
                                    if (
                                        delta?.get("type")
                                            ?.jsonPrimitive
                                            ?.contentOrNull == "text"
                                    ) {
                                        delta["text"]
                                            ?.jsonPrimitive
                                            ?.contentOrNull
                                            ?.takeIf { it.isNotEmpty() }
                                            ?.let { text ->
                                                trySend(ChatChunk(text))
                                            }
                                    }
                                }

                                "interaction.completed" -> {
                                    trySend(ChatChunk("", true))
                                }

                                "error" -> {
                                    val message = event["error"]
                                        ?.jsonObject
                                        ?.get("message")
                                        ?.jsonPrimitive
                                        ?.contentOrNull
                                        ?: "O Gemini informou uma falha."
                                    close(IOException(message))
                                    return
                                }
                            }
                        }
                    } catch (t: Throwable) {
                        close(t)
                    } finally {
                        response.close()
                        close()
                    }
                }
            }
        )

        awaitClose { call.cancel() }
    }

    private fun friendlyError(
        code: Int,
        detail: String
    ): String = when (code) {
        400 -> "O Gemini recusou a solicitação. Confira o modelo selecionado."
        401, 403 -> "Chave do Gemini inválida ou sem permissão."
        404 -> "O modelo Gemini selecionado não foi encontrado."
        429 -> "A cota gratuita do Gemini foi atingida. Tente novamente mais tarde."
        else -> "Erro do Gemini (HTTP $code): $detail"
    }
}
