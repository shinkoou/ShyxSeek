package com.shyxseek.app.tools

import com.shyxseek.app.domain.PermissionLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable data class ToolDefinition(val name:String,val description:String,val permissionLevel:PermissionLevel,val requiresNetwork:Boolean=false,val requiresConfirmation:Boolean=false,val category:String="general")
data class ToolExecutionContext(val projectId:Long?,val conversationId:Long)
data class ToolResult(val success:Boolean,val displayText:String,val data:JsonObject?=null,val error:String?=null)
interface Tool{val definition:ToolDefinition; suspend fun execute(arguments:JsonObject,context:ToolExecutionContext):ToolResult}
class ToolRegistry(tools:Set<Tool>){private val map=tools.associateBy{it.definition.name};fun definitions()=map.values.map{it.definition};fun get(name:String)=map[name];suspend fun execute(name:String,args:JsonObject,ctx:ToolExecutionContext)=map[name]?.execute(args,ctx)?:ToolResult(false,"Ferramenta indisponível",error="NOT_INSTALLED")}
