package com.shyxseek.app.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.ai.LocalAIProvider
import com.shyxseek.app.ai.LocalModelManager
import com.shyxseek.app.ai.LocalModelState
import com.shyxseek.app.settings.AppSettings
import com.shyxseek.app.settings.FAKE_PROVIDER_ID
import com.shyxseek.app.settings.GEMINI_BASE_URL
import com.shyxseek.app.settings.GEMINI_DEFAULT_MODEL
import com.shyxseek.app.settings.OPENAI_BASE_URL
import com.shyxseek.app.settings.OPENAI_DEFAULT_MODEL
import com.shyxseek.app.settings.ProviderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

data class ConnectionTestState(
    val testing: Boolean = false,
    val success: Boolean? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AppSettings,
    private val client: OkHttpClient,
    private val localModels: LocalModelManager,
    private val localProvider: LocalAIProvider
) : ViewModel() {

    val state = settings.flow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProviderSettings()
    )

    val localState: StateFlow<LocalModelState> = localModels.state

    private val _connection = MutableStateFlow(ConnectionTestState())
    val connection: StateFlow<ConnectionTestState> = _connection.asStateFlow()

    init {
        localModels.refresh()
    }

    fun useOffline() {
        viewModelScope.launch {
            settings.setProvider(FAKE_PROVIDER_ID)
            _connection.value = ConnectionTestState(
                success = true,
                message = "Modo offline de teste ativado."
            )
        }
    }

    fun useLocal(backend: String) {
        if (!localState.value.installed) {
            _connection.value = ConnectionTestState(
                success = false,
                message = "Instale ou importe um modelo local primeiro."
            )
            return
        }

        viewModelScope.launch {
            localProvider.reset()
            settings.setLocal(backend)
            _connection.value = ConnectionTestState(
                success = true,
                message = if (backend == "gpu") {
                    "IA local ativada com GPU."
                } else {
                    "IA local ativada com CPU."
                }
            )
        }
    }

    fun downloadLocalModel() {
        localProvider.reset()
        localModels.downloadRecommended()
    }

    fun importLocalModel(uri: Uri) {
        localProvider.reset()
        localModels.importModel(uri)
    }

    fun deleteLocalModel() {
        localProvider.reset()
        localModels.deleteModel()
    }

    fun saveGemini(model: String, key: String) {
        viewModelScope.launch {
            val hasKey = key.isNotBlank() || !settings.geminiKey().isNullOrBlank()

            if (!hasKey) {
                _connection.value = ConnectionTestState(
                    success = false,
                    message = "Cole sua chave gratuita do Gemini primeiro."
                )
                return@launch
            }

            settings.setGemini(
                model = model.ifBlank { GEMINI_DEFAULT_MODEL },
                apiKey = key
            )

            _connection.value = ConnectionTestState(
                success = true,
                message = "Gemini selecionado. Você já pode voltar ao chat."
            )
        }
    }

    fun testGemini(model: String, typedKey: String) {
        val key = typedKey.trim().ifBlank {
            settings.geminiKey().orEmpty()
        }

        if (key.isBlank()) {
            _connection.value = ConnectionTestState(
                success = false,
                message = "Cole sua chave gratuita do Gemini primeiro."
            )
            return
        }

        _connection.value = ConnectionTestState(testing = true)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val target = model.ifBlank { GEMINI_DEFAULT_MODEL }
                    val request = Request.Builder()
                        .url("$GEMINI_BASE_URL/v1beta/models/$target")
                        .header("x-goog-api-key", key)
                        .get()
                        .build()

                    client.newCall(request).execute().use { response ->
                        when {
                            response.isSuccessful -> Pair(
                                true,
                                "Conexão com o Gemini funcionando."
                            )
                            response.code == 401 || response.code == 403 -> Pair(
                                false,
                                "Chave do Gemini inválida ou sem permissão."
                            )
                            response.code == 404 -> Pair(
                                false,
                                "Modelo Gemini indisponível nessa conta."
                            )
                            response.code == 429 -> Pair(
                                false,
                                "Sua cota gratuita do Gemini foi atingida."
                            )
                            else -> Pair(
                                false,
                                "O Gemini respondeu com HTTP ${response.code}."
                            )
                        }
                    }
                }.getOrElse { error ->
                    Pair(
                        false,
                        "Falha de conexão: ${error.message ?: "erro desconhecido"}"
                    )
                }
            }

            _connection.value = ConnectionTestState(
                testing = false,
                success = result.first,
                message = result.second
            )
        }
    }

    fun saveOpenAI(model: String, key: String) {
        viewModelScope.launch {
            val hasKey = key.isNotBlank() || !settings.openAiKey().isNullOrBlank()

            if (!hasKey) {
                _connection.value = ConnectionTestState(
                    success = false,
                    message = "Cole sua API key da OpenAI primeiro."
                )
                return@launch
            }

            settings.setOpenAI(
                model = model.ifBlank { OPENAI_DEFAULT_MODEL },
                apiKey = key
            )

            _connection.value = ConnectionTestState(
                success = true,
                message = "OpenAI selecionada. Você já pode voltar ao chat."
            )
        }
    }

    fun testOpenAI(model: String, typedKey: String) {
        val key = typedKey.trim().ifBlank {
            settings.openAiKey().orEmpty()
        }

        if (key.isBlank()) {
            _connection.value = ConnectionTestState(
                success = false,
                message = "Cole sua API key da OpenAI primeiro."
            )
            return
        }

        _connection.value = ConnectionTestState(testing = true)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val target = model.ifBlank { OPENAI_DEFAULT_MODEL }
                    val request = Request.Builder()
                        .url("$OPENAI_BASE_URL/v1/models/$target")
                        .header("Authorization", "Bearer $key")
                        .get()
                        .build()

                    client.newCall(request).execute().use { response ->
                        when {
                            response.isSuccessful -> Pair(
                                true,
                                "Conexão com a OpenAI funcionando."
                            )
                            response.code == 401 -> Pair(false, "API key inválida.")
                            response.code == 403 -> Pair(
                                false,
                                "Sua conta não tem acesso a este modelo."
                            )
                            response.code == 404 -> Pair(
                                false,
                                "Este modelo não está disponível na sua conta."
                            )
                            response.code == 429 -> Pair(
                                false,
                                "Limite ou saldo da API atingido."
                            )
                            else -> Pair(
                                false,
                                "A OpenAI respondeu com HTTP ${response.code}."
                            )
                        }
                    }
                }.getOrElse { error ->
                    Pair(
                        false,
                        "Falha de conexão: ${error.message ?: "erro desconhecido"}"
                    )
                }
            }

            _connection.value = ConnectionTestState(
                testing = false,
                success = result.first,
                message = result.second
            )
        }
    }

    fun clearConnectionMessage() {
        _connection.value = ConnectionTestState()
    }
}
