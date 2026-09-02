package com.shyxseek.app.agent

import com.shyxseek.app.ai.AIProviderRegistry
import com.shyxseek.app.domain.*
import com.shyxseek.app.rag.RetrievalEngine
import com.shyxseek.app.settings.AppSettings
import com.shyxseek.app.tools.ToolRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AgentOrchestrator(private val providers:AIProviderRegistry,private val retrieval:RetrievalEngine,private val prompts:SystemPromptBuilder,private val tools:ToolRegistry,private val settings:AppSettings){
 suspend fun stream(messages:List<ChatMessage>,projectId:Long?,projectName:String?):Flow<ChatChunk>{
   val query=messages.lastOrNull()?.content.orEmpty(); val hits=retrieval.search(query,projectId); val cfg=settings.flow.first(); val provider=providers.get(cfg.provider)
   val system=prompts.build(projectName,hits,tools.definitions()); return provider.streamChat(ChatRequest(messages,system,cfg.model.ifBlank{"default"},cfg.temperature))
 }
}
