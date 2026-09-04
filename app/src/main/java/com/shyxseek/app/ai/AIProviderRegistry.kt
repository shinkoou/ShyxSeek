package com.shyxseek.app.ai

class AIProviderRegistry(
    private val fake: FakeAIProvider,
    private val local: LocalAIProvider,
    private val gemini: GeminiProvider,
    private val openAI: OpenAICompatibleProvider
) {
    fun get(id: String): AIProvider = when (id) {
        "local_litert" -> local
        "gemini_free" -> gemini
        "openai_compatible" -> openAI
        else -> fake
    }

    fun all(): List<AIProvider> =
        listOf(fake, local, gemini, openAI)
}
