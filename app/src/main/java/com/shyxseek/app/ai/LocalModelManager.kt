package com.shyxseek.app.ai

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class LocalModelState(
    val installed: Boolean = false,
    val busy: Boolean = false,
    val progress: Int = -1,
    val sizeBytes: Long = 0L,
    val modelName: String = "Qwen3 0.6B INT4",
    val message: String? = null
)

class LocalModelManager(
    private val context: Context,
    private val client: OkHttpClient
) {
    companion object {
        const val RECOMMENDED_FILE =
            "qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm"

        const val RECOMMENDED_URL =
            "https://huggingface.co/litert-community/Qwen3-0.6B-int4/resolve/main/" +
                "qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm?download=true"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val modelDir = File(context.filesDir, "models").apply { mkdirs() }
    private val modelFile = File(modelDir, "shyxseek-local.litertlm")
    private val partialFile = File(modelDir, "shyxseek-local.part")

    private val _state = MutableStateFlow(currentState())
    val state: StateFlow<LocalModelState> = _state.asStateFlow()

    fun modelPath(): String? =
        modelFile.takeIf { it.exists() && it.length() > 0L }?.absolutePath

    fun refresh() {
        _state.value = currentState()
    }

    fun downloadRecommended() {
        if (_state.value.busy) return

        scope.launch {
            partialFile.delete()
            _state.value = currentState().copy(
                busy = true,
                progress = 0,
                message = "Baixando modelo recomendado…"
            )

            runCatching {
                val request = Request.Builder()
                    .url(RECOMMENDED_URL)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        error("Download falhou: HTTP ${response.code}")
                    }

                    val body = response.body
                    val total = body.contentLength()
                    var copied = 0L

                    body.byteStream().use { input ->
                        partialFile.outputStream().buffered().use { output ->
                            val buffer = ByteArray(256 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read <= 0) break
                                output.write(buffer, 0, read)
                                copied += read

                                val progress = if (total > 0L) {
                                    ((copied * 100L) / total)
                                        .coerceIn(0L, 100L)
                                        .toInt()
                                } else {
                                    -1
                                }

                                _state.value = _state.value.copy(
                                    progress = progress,
                                    sizeBytes = copied
                                )
                            }
                        }
                    }
                }

                if (!partialFile.exists() || partialFile.length() < 10_000_000L) {
                    error("O arquivo baixado parece incompleto.")
                }

                modelFile.delete()
                if (!partialFile.renameTo(modelFile)) {
                    partialFile.copyTo(modelFile, overwrite = true)
                    partialFile.delete()
                }

                _state.value = currentState().copy(
                    message = "Modelo local instalado e pronto."
                )
            }.onFailure { error ->
                partialFile.delete()
                _state.value = currentState().copy(
                    message = error.message ?: "Não foi possível baixar o modelo."
                )
            }
        }
    }

    fun importModel(uri: Uri) {
        if (_state.value.busy) return

        scope.launch {
            _state.value = currentState().copy(
                busy = true,
                progress = -1,
                message = "Importando modelo local…"
            )

            runCatching {
                val input = context.contentResolver.openInputStream(uri)
                    ?: error("Não foi possível abrir o arquivo.")

                partialFile.delete()

                input.use { source ->
                    partialFile.outputStream().buffered().use { target ->
                        source.copyTo(target, bufferSize = 256 * 1024)
                    }
                }

                if (!partialFile.exists() || partialFile.length() < 10_000_000L) {
                    error("Esse arquivo não parece ser um modelo LiteRT-LM válido.")
                }

                modelFile.delete()
                if (!partialFile.renameTo(modelFile)) {
                    partialFile.copyTo(modelFile, overwrite = true)
                    partialFile.delete()
                }

                _state.value = currentState().copy(
                    message = "Modelo importado e pronto."
                )
            }.onFailure { error ->
                partialFile.delete()
                _state.value = currentState().copy(
                    message = error.message ?: "Falha ao importar o modelo."
                )
            }
        }
    }

    fun deleteModel() {
        if (_state.value.busy) return
        modelFile.delete()
        partialFile.delete()
        _state.value = currentState().copy(
            message = "Modelo local removido."
        )
    }

    private fun currentState(): LocalModelState {
        val installed = modelFile.exists() && modelFile.length() > 0L
        return LocalModelState(
            installed = installed,
            busy = false,
            progress = if (installed) 100 else -1,
            sizeBytes = if (installed) modelFile.length() else 0L,
            message = null
        )
    }
}
