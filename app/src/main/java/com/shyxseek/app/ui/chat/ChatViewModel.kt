package com.shyxseek.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.settings.AppSettings
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
    private val client: OkHttpClient
) : ViewModel() {

    val state = settings.flow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProviderSettings()
    )

    private val _connection = MutableStateFlow(ConnectionTestState())
    val connection: StateFlow<ConnectionTestState> = _connection.asStateFlow()

    fun useOffline() {
        viewModelScope.launch {
            settings.save(
                ProviderSettings(
                    provider = "fake",
                    baseUrl = OPENAI_BASE_URL,
                    model = state.value.model.ifBlank { OPENAI_DEFAULT_MODEL },
                    temperature = state.value.temperature,
                    hasApiKey = state.value.hasApiKey
                ),
                apiKey = null
            )
            _connection.value = ConnectionTestState(
                success = true,
                message = "Modo offline de teste ativado."
            )
        }
    }

    fun saveOpenAI(model: String, key: String) {
        viewModelScope.launch {
            val hasKey = key.isNotBlank() || !settings.apiKey().isNullOrBlank()

            settings.save(
                ProviderSettings(
                    provider = "openai_compatible",
                    baseUrl = OPENAI_BASE_URL,
                    model = model.ifBlank { OPENAI_DEFAULT_MODEL },
                    temperature = state.value.temperature,
                    hasApiKey = hasKey
                ),
                apiKey = key
            )

            _connection.value = ConnectionTestState(
                success = if (hasKey) true else null,
                message = if (hasKey) {
                    "OpenAI selecionada. Você já pode voltar ao chat."
                } else {
                    "Cole sua API key para usar a OpenAI."
                }
            )
        }
    }

    fun testOpenAI(model: String, typedKey: String) {
        val key = typedKey.trim().ifBlank {
            settings.apiKey().orEmpty()
        }

        if (key.isBlank()) {
            _connection.value = ConnectionTestState(
                success = false,
                message = "Cole sua API key primeiro."
            )
            return
        }

        _connection.value = ConnectionTestState(testing = true)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val targetModel = model.ifBlank { OPENAI_DEFAULT_MODEL }
                    val request = Request.Builder()
                        .url("$OPENAI_BASE_URL/v1/models/$targetModel")
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

    fun save(provider: String, base: String, model: String, key: String) {
        viewModelScope.launch {
            settings.save(
                ProviderSettings(
                    provider = provider,
                    baseUrl = base,
                    model = model,
                    temperature = state.value.temperature,
                    hasApiKey = key.isNotBlank() || state.value.hasApiKey
                ),
                key
            )
        }
    }
}
