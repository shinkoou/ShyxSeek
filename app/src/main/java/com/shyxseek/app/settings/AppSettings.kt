package com.shyxseek.app.settings

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shyxseek.app.security.SecretStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("shyxseek_settings")

const val OPENAI_BASE_URL = "https://api.openai.com"
const val OPENAI_DEFAULT_MODEL = "gpt-5.6-luna"

const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com"
const val GEMINI_DEFAULT_MODEL = "gemini-3.1-flash-lite"

const val LOCAL_PROVIDER_ID = "local_litert"
const val GEMINI_PROVIDER_ID = "gemini_free"
const val OPENAI_PROVIDER_ID = "openai_compatible"
const val FAKE_PROVIDER_ID = "fake"

data class ProviderSettings(
    val provider: String = FAKE_PROVIDER_ID,
    val model: String = "fake",
    val temperature: Double = 0.4,
    val openAiModel: String = OPENAI_DEFAULT_MODEL,
    val geminiModel: String = GEMINI_DEFAULT_MODEL,
    val localBackend: String = "cpu",
    val hasOpenAiKey: Boolean = false,
    val hasGeminiKey: Boolean = false
)

class AppSettings(
    private val context: Context,
    private val secrets: SecretStore
) {
    private val providerKey = stringPreferencesKey("provider")
    private val openAiModelKey = stringPreferencesKey("openai_model")
    private val geminiModelKey = stringPreferencesKey("gemini_model")
    private val localBackendKey = stringPreferencesKey("local_backend")
    private val tempKey = doublePreferencesKey("temperature")

    val flow: Flow<ProviderSettings> = context.dataStore.data.map { prefs ->
        val provider = prefs[providerKey] ?: FAKE_PROVIDER_ID
        val openAiModel = prefs[openAiModelKey]
            ?.takeIf { it.isNotBlank() }
            ?: OPENAI_DEFAULT_MODEL
        val geminiModel = prefs[geminiModelKey]
            ?.takeIf { it.isNotBlank() }
            ?: GEMINI_DEFAULT_MODEL

        ProviderSettings(
            provider = provider,
            model = when (provider) {
                OPENAI_PROVIDER_ID -> openAiModel
                GEMINI_PROVIDER_ID -> geminiModel
                LOCAL_PROVIDER_ID -> "qwen3-0.6b-local"
                else -> "fake"
            },
            temperature = prefs[tempKey] ?: 0.4,
            openAiModel = openAiModel,
            geminiModel = geminiModel,
            localBackend = prefs[localBackendKey] ?: "cpu",
            hasOpenAiKey = !openAiKey().isNullOrBlank(),
            hasGeminiKey = !geminiKey().isNullOrBlank()
        )
    }

    suspend fun setProvider(provider: String) {
        context.dataStore.edit { prefs ->
            prefs[providerKey] = provider
        }
    }

    suspend fun setLocal(backend: String) {
        context.dataStore.edit { prefs ->
            prefs[providerKey] = LOCAL_PROVIDER_ID
            prefs[localBackendKey] = backend
        }
    }

    suspend fun setGemini(model: String, apiKey: String?) {
        context.dataStore.edit { prefs ->
            prefs[providerKey] = GEMINI_PROVIDER_ID
            prefs[geminiModelKey] = model.ifBlank { GEMINI_DEFAULT_MODEL }
        }
        if (!apiKey.isNullOrBlank()) {
            secrets.put("gemini_api_key", apiKey.trim())
        }
    }

    suspend fun setOpenAI(model: String, apiKey: String?) {
        context.dataStore.edit { prefs ->
            prefs[providerKey] = OPENAI_PROVIDER_ID
            prefs[openAiModelKey] = model.ifBlank { OPENAI_DEFAULT_MODEL }
        }
        if (!apiKey.isNullOrBlank()) {
            secrets.put("openai_api_key", apiKey.trim())
        }
    }

    suspend fun setTemperature(value: Double) {
        context.dataStore.edit { prefs ->
            prefs[tempKey] = value
        }
    }

    fun openAiKey(): String? =
        secrets.get("openai_api_key")
            ?: secrets.get("api_key") // compatibilidade com builds anteriores

    fun geminiKey(): String? = secrets.get("gemini_api_key")

    // Compatibilidade com o provider OpenAI já existente.
    fun apiKey(): String? = openAiKey()
}
