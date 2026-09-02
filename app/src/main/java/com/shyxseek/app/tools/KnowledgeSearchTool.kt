package com.shyxseek.app.tools
import com.shyxseek.app.domain.PermissionLevel
import com.shyxseek.app.rag.RetrievalEngine
import kotlinx.serialization.json.*
class KnowledgeSearchTool(private val retrieval:RetrievalEngine):Tool{
 override val definition=ToolDefinition("knowledge_search","Busca memória e conhecimento local",PermissionLevel.SAFE_READ)
 override suspend fun execute(arguments:JsonObject,context:ToolExecutionContext):ToolResult{
   val q=arguments["query"]?.jsonPrimitive?.contentOrNull?:return ToolResult(false,"Consulta ausente",error="INVALID_ARGUMENT")
   val hits=retrieval.search(q,context.projectId,6); return ToolResult(true,hits.joinToString("\n"){"• [${it.type}] ${it.text.take(240)}"})
 }
}
