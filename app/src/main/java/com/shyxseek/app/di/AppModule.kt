package com.shyxseek.app.di

import android.content.Context
import androidx.room.Room
import com.shyxseek.app.ai.*
import com.shyxseek.app.agent.*
import com.shyxseek.app.data.local.*
import com.shyxseek.app.data.repository.*
import com.shyxseek.app.rag.*
import com.shyxseek.app.security.SecretStore
import com.shyxseek.app.settings.AppSettings
import com.shyxseek.app.tools.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object AppModule{
 @Provides @Singleton fun db(@ApplicationContext c:Context)=Room.databaseBuilder(c,AppDatabase::class.java,"shyxseek.db").build()
 @Provides fun conversations(db:AppDatabase)=db.conversations(); @Provides fun messages(db:AppDatabase)=db.messages(); @Provides fun knowledge(db:AppDatabase)=db.knowledge(); @Provides fun memories(db:AppDatabase)=db.memories(); @Provides fun projects(db:AppDatabase)=db.projects()
 @Provides @Singleton fun secret(@ApplicationContext c:Context)=SecretStore(c)
 @Provides @Singleton fun settings(@ApplicationContext c:Context,s:SecretStore)=AppSettings(c,s)
 @Provides @Singleton fun http()=OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply{level=HttpLoggingInterceptor.Level.BASIC}).build()
 @Provides @Singleton fun embedding():EmbeddingProvider=HashEmbeddingProvider()
 @Provides @Singleton fun retrieval(k:KnowledgeDao,m:MemoryDao,e:EmbeddingProvider)=RetrievalEngine(k,m,e)
 @Provides @Singleton fun conversationRepo(c:ConversationDao,m:MessageDao)=ConversationRepository(c,m)
 @Provides @Singleton fun knowledgeRepo(k:KnowledgeDao,r:RetrievalEngine)=KnowledgeRepository(k,r)
 @Provides @Singleton fun memoryRepo(m:MemoryDao,r:RetrievalEngine)=MemoryRepository(m,r)
 @Provides @Singleton fun projectRepo(p:ProjectDao)=ProjectRepository(p)
 @Provides @Singleton fun fake()=FakeAIProvider()
 @Provides @Singleton fun compatible(c:OkHttpClient,s:AppSettings)=OpenAICompatibleProvider(c,s)
 @Provides @Singleton fun providers(f:FakeAIProvider,c:OpenAICompatibleProvider)=AIProviderRegistry(f,c)
 @Provides @Singleton fun knowledgeTool(r:RetrievalEngine)=KnowledgeSearchTool(r)
 @Provides @Singleton fun toolSet(k:KnowledgeSearchTool):Set<Tool> = setOf(k)
 @Provides @Singleton fun registry(t:Set<@JvmSuppressWildcards Tool>)=ToolRegistry(t)
 @Provides @Singleton fun prompts()=SystemPromptBuilder()
 @Provides @Singleton fun orchestrator(p:AIProviderRegistry,r:RetrievalEngine,s:SystemPromptBuilder,t:ToolRegistry,a:AppSettings)=AgentOrchestrator(p,r,s,t,a)
}
