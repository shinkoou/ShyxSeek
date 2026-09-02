package com.shyxseek.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.agent.AgentOrchestrator
import com.shyxseek.app.data.local.MessageEntity
import com.shyxseek.app.data.repository.ConversationRepository
import com.shyxseek.app.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(val conversationId:Long?=null,val messages:List<MessageEntity> = emptyList(),val draft:String="",val generating:Boolean=false,val error:String?=null)
@HiltViewModel class ChatViewModel @Inject constructor(private val repo:ConversationRepository,private val agent:AgentOrchestrator):ViewModel(){
 private val _state=MutableStateFlow(ChatUiState()); val state:StateFlow<ChatUiState> = _state.asStateFlow(); private var observe:Job?=null; private var generation:Job?=null
 init{viewModelScope.launch{open(repo.create())}}
 fun draft(v:String){_state.update{it.copy(draft=v)}}
 private fun open(id:Long){observe?.cancel();_state.update{it.copy(conversationId=id)};observe=viewModelScope.launch{repo.messages(id).collect{m->_state.update{it.copy(messages=m)}}}}
 fun send(){val text=state.value.draft.trim();val id=state.value.conversationId?:return;if(text.isBlank()||state.value.generating)return;_state.update{it.copy(draft="",generating=true,error=null)};generation=viewModelScope.launch{
   try{repo.add(id,MessageRole.USER,text); val history=repo.list(id).map{ChatMessage(it.role,it.content)}; val sb=StringBuilder(); agent.stream(history,null,null).collect{chunk-> if(chunk.text.isNotEmpty())sb.append(chunk.text)}; repo.add(id,MessageRole.ASSISTANT,sb.toString())}
   catch(t:Throwable){_state.update{it.copy(error=t.message?:"Falha desconhecida")}} finally{_state.update{it.copy(generating=false)}}
 }}
 fun stop(){generation?.cancel();_state.update{it.copy(generating=false)}}
}
