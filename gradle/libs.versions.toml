package com.shyxseek.app.ai

import com.shyxseek.app.domain.*
import com.shyxseek.app.settings.AppSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class OpenAICompatibleProvider(
    private val client: OkHttpClient,
    private val settings: AppSettings
) : AIProvider {

    override val id = "openai_compatible"
    override val displayName = "OpenAI Compatible"

    override val capabilities = ProviderCapabilities(
        setOf(
            Capability.TEXT_GENERATION,
            Capability.STREAMING
        )
    )

    @Serializable
    private data class Req(
        val model: String,
        val messages: List<Msg>,
        val stream: Boolean = true,
        val temperature: Double = 0.4
    )

    @Serializable
    private data class Msg(
        val role: String,
        val content: String
    )

    override fun streamChat(request: ChatRequest): Flow<ChatChunk> = callbackFlow {
        val current = settings.flow.first()
        val key = settings.apiKey()

        if (key.isNullOrBlank()) {
            close(IllegalStateException("API key não configurada"))
            return@callbackFlow
        }

        val msgs = buildList {
            add(Msg("system", request.systemPrompt))
            request.messages.forEach {
                add(Msg(it.role.name.lowercase(), it.content))
            }
        }

        val body = Json.encodeToString(
            Req(
                model = current.model.ifBlank { request.modelId },
                messages = msgs,
                stream = true,
                temperature = current.temperature
            )
        ).toRequestBody("application/json".toMediaType())

        val url = current.baseUrl.trimEnd('/') + "/v1/chat/completions"

        val call = client.newCall(
            Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $key")
                .header("Accept", "text/event-stream")
                .post(body)
                .build()
        )

        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    close(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        close(
                            IOException(
                                "HTTP ${response.code}: ${
                                    response.body.string().take(400)
                                }"
                            )
                        )
                        response.close()
                        return
                    }

                    try {
                        val source = response.body.source()

                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break

                            if (!line.startsWith("data:")) {
                                continue
                            }

                            val data = line.removePrefix("data:").trim()

                            if (data == "[DONE]") {
                                trySend(ChatChunk("", true))
                                break
                            }

                            runCatching {
                                Json.parseToJsonElement(data)
                                    .jsonObject["choices"]
                                    ?.jsonArray
                                    ?.firstOrNull()
                                    ?.jsonObject
                                    ?.get("delta")
                                    ?.jsonObject
                                    ?.get("content")
                                    ?.jsonPrimitive
                                    ?.contentOrNull
                            }.getOrNull()?.let {
                                trySend(ChatChunk(it))
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
}
