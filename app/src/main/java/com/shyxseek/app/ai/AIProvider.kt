package com.shyxseek.app.ai

import com.shyxseek.app.domain.*
import kotlinx.coroutines.flow.Flow

interface AIProvider {
 val id:String
 val displayName:String
 val capabilities:ProviderCapabilities
 fun streamChat(request:ChatRequest):Flow<ChatChunk>
}
