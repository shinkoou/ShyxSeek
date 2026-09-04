package com.shyxseek.app.ai

import com.shyxseek.app.domain.Capability
import com.shyxseek.app.domain.ChatChunk
import com.shyxseek.app.domain.ChatRequest
import com.shyxseek.app.domain.MessageRole
import com.shyxseek.app.domain.ProviderCapabilities
import com.shyxseek.app.settings.AppSettings
import com.shyxseek.app.settings.OPENAI_BASE_URL
import com.shyxseek.app.settings.OPENAI_DEFAULT_MODEL
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class OpenAICompatibleProvider(
    private val client: OkHttpClient,
    private val settings: AppSettings
) : AIProvider {

    override val id = "openai_compatible"
    override val displayName = "OpenAI"

    override val capabilities = ProviderCapabilities(
        setOf(
            Capability.TEXT_GENERATION,
            Capability.STREAMING
        )
    )

    @Serializable
    private data class ResponsesRequest(
        val model: String,
        val instructions: String,
        val input: String,
        val stream: Boolean = true,
        val max_output_tokens: Int = 2048
    )

    override fun streamChat(request: ChatRequest): Flow<ChatChunk> = callbackFlow {
        val current = settings.flow.first()
        val key = settings.apiKey()

        if (key.isNullOrBlank()) {
            close(
                IllegalStateException(
                    "A OpenAI ainda não está conectada. Abra Ajustes e adicione sua API key."
                )
            )
            return@callbackFlow
        }

        val model = current.model.ifBlank { OPENAI_DEFAULT_MODEL }

        val transcript = request.messages.joinToString(separator = "\n\n") { message ->
            val role = when (message.role) {
                MessageRole.USER -> "Usuário"
                MessageRole.ASSISTANT -> "ShyxSeek"
                MessageRole.SYSTEM -> "Sistema"
                MessageRole.TOOL -> "Ferramenta"
            }
            "$role: ${message.content}"
        }.ifBlank {
            "Usuário: Olá."
        }

        val payload = ResponsesRequest(
            model = model,
            instructions = request.systemPrompt,
            input = transcript,
            stream = true,
            max_output_tokens = request.maxOutputTokens
        )

        val body = Json.encodeToString(payload)
            .toRequestBody("application/json".toMediaType())

        val baseUrl = current.baseUrl
            .ifBlank { OPENAI_BASE_URL }
            .trimEnd('/')

        val call = client.newCall(
            Request.Builder()
                .url("$baseUrl/v1/responses")
                .header("Authorization", "Bearer $key")
                .header("Accept", "text/event-stream")
                .post(body)
                .build()
        )

        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    close(
                        IOException(
                            "Não foi possível conectar à OpenAI: ${e.message}",
                            e
                        )
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        val detail = response.body.string().take(600)
                        val message = friendlyHttpError(response.code, detail)
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

                            when (event["type"]?.jsonPrimitive?.contentOrNull) {
                                "response.output_text.delta" -> {
                                    event["delta"]
                                        ?.jsonPrimitive
                                        ?.contentOrNull
                                        ?.takeIf { it.isNotEmpty() }
                                        ?.let { delta ->
                                            trySend(ChatChunk(delta))
                                        }
                                }

                                "response.completed" -> {
                                    trySend(ChatChunk("", true))
                                }

                                "response.failed",
                                "error" -> {
                                    val message = event["message"]
                                        ?.jsonPrimitive
                                        ?.contentOrNull
                                        ?: "A OpenAI informou uma falha ao gerar a resposta."
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

        awaitClose {
            call.cancel()
        }
    }

    private fun friendlyHttpError(code: Int, detail: String): String = when (code) {
        401 -> "API key da OpenAI inválida. Confira a chave em Ajustes."
        403 -> "Sua conta da OpenAI não tem acesso a este recurso ou modelo."
        404 -> "O modelo selecionado não foi encontrado na sua conta da OpenAI."
        429 -> "A OpenAI recusou a solicitação por limite de uso ou saldo da API."
        else -> "Erro da OpenAI (HTTP $code): $detail"
    }
}
