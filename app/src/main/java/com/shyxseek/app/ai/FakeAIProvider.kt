package com.shyxseek.app.ai

import com.shyxseek.app.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAIProvider:AIProvider{
 override val id="fake"
 override val displayName="Fake AI (offline)"
 override val capabilities=ProviderCapabilities(setOf(Capability.TEXT_GENERATION,Capability.STREAMING))
 override fun streamChat(request:ChatRequest):Flow<ChatChunk> = flow {
   val last=request.messages.lastOrNull()?.content.orEmpty()
   val text="Modo local de teste ativo. Você disse: “${last.take(240)}”. Configure um provider compatível em Ajustes para usar um modelo real."
   text.chunked(7).forEach{delay(25); emit(ChatChunk(it))}; emit(ChatChunk("",true))
 }
}
