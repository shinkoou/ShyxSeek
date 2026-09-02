package com.shyxseek.app.data.repository

import com.shyxseek.app.data.local.*
import com.shyxseek.app.domain.*
import com.shyxseek.app.rag.RetrievalEngine
import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val conversations:ConversationDao,private val messages:MessageDao){
 fun conversations():Flow<List<ConversationEntity>> = conversations.observeAll()
 fun messages(id:Long):Flow<List<MessageEntity>> = messages.observe(id)
 suspend fun create(title:String="Nova conversa",projectId:Long?=null):Long { val now=System.currentTimeMillis(); return conversations.insert(ConversationEntity(title=title,projectId=projectId,createdAt=now,updatedAt=now)) }
 suspend fun add(conversationId:Long,role:MessageRole,content:String,modelId:String?=null)=messages.insert(MessageEntity(conversationId=conversationId,role=role,content=content,timestamp=System.currentTimeMillis(),modelId=modelId))
 suspend fun list(id:Long)=messages.list(id)
}
class KnowledgeRepository(private val dao:KnowledgeDao,private val retrieval:RetrievalEngine){
 fun observeAll()=dao.observeAll()
 suspend fun teach(topic:String,content:String,projectId:Long?=null):Long{val now=System.currentTimeMillis();return dao.insert(KnowledgeEntity(topic=topic,content=content,source=KnowledgeSource.USER_TAUGHT,embeddingJson=retrieval.encode(content),projectId=projectId,createdAt=now,updatedAt=now))}
}
class MemoryRepository(private val dao:MemoryDao,private val retrieval:RetrievalEngine){
 fun observeAll()=dao.observeAll()
 suspend fun remember(content:String,type:MemoryType=MemoryType.LONG_TERM,projectId:Long?=null):Long{val now=System.currentTimeMillis();return dao.insert(MemoryEntity(memoryType=type,content=content,embeddingJson=retrieval.encode(content),projectId=projectId,createdAt=now,updatedAt=now,isSensitive=type==MemoryType.SENSITIVE))}
}
class ProjectRepository(private val dao:ProjectDao){ fun observeAll()=dao.observeAll(); suspend fun ensureDefault(){if(dao.get(1)==null){val n=System.currentTimeMillis();dao.upsert(ProjectEntity(1,"ShyxSeek","Assistente IA pessoal extensível","initial development","Base Android funcional",createdAt=n,updatedAt=n))}} }
