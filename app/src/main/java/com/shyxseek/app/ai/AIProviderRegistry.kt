package com.shyxseek.app.ai

class AIProviderRegistry(private val fake:FakeAIProvider,private val compatible:OpenAICompatibleProvider){
 fun get(id:String):AIProvider=when(id){"openai_compatible"->compatible;else->fake}
 fun all():List<AIProvider> = listOf(fake,compatible)
}
