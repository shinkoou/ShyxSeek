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

data class ProviderSettings(
    val provider: String = "fake",
    val baseUrl: String = OPENAI_BASE_URL,
    val model: String = OPENAI_DEFAULT_MODEL,
    val temperature: Double = 0.4,
    val hasApiKey: Boolean = false
)

class AppSettings(
    private val context: Context,
    private val secrets: SecretStore
) {
    private val provider = stringPreferencesKey("provider")
    private val base = stringPreferencesKey("base_url")
    private val model = stringPreferencesKey("model")
    private val temp = doublePreferencesKey("temperature")

    val flow: Flow<ProviderSettings> = context.dataStore.data.map { prefs ->
        ProviderSettings(
            provider = prefs[provider] ?: "fake",
            baseUrl = prefs[base] ?: OPENAI_BASE_URL,
            model = prefs[model]?.takeIf { it.isNotBlank() } ?: OPENAI_DEFAULT_MODEL,
            temperature = prefs[temp] ?: 0.4,
            hasApiKey = !secrets.get("api_key").isNullOrBlank()
        )
    }

    suspend fun save(value: ProviderSettings, apiKey: String?) {
        context.dataStore.edit { prefs ->
            prefs[provider] = value.provider
            prefs[base] = value.baseUrl
            prefs[model] = value.model
            prefs[temp] = value.temperature
        }

        if (!apiKey.isNullOrBlank()) {
            secrets.put("api_key", apiKey.trim())
        }
    }

    fun apiKey(): String? = secrets.get("api_key")
}
