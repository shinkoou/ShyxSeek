package com.shyxseek.app.di

import android.content.Context
import androidx.room.Room
import com.shyxseek.app.ai.AIProviderRegistry
import com.shyxseek.app.ai.FakeAIProvider
import com.shyxseek.app.ai.GeminiProvider
import com.shyxseek.app.ai.LocalAIProvider
import com.shyxseek.app.ai.LocalModelManager
import com.shyxseek.app.ai.OpenAICompatibleProvider
import com.shyxseek.app.agent.AgentOrchestrator
import com.shyxseek.app.agent.SystemPromptBuilder
import com.shyxseek.app.data.local.AppDatabase
import com.shyxseek.app.data.local.ConversationDao
import com.shyxseek.app.data.local.KnowledgeDao
import com.shyxseek.app.data.local.MemoryDao
import com.shyxseek.app.data.local.MessageDao
import com.shyxseek.app.data.local.ProjectDao
import com.shyxseek.app.data.repository.ConversationRepository
import com.shyxseek.app.data.repository.KnowledgeRepository
import com.shyxseek.app.data.repository.MemoryRepository
import com.shyxseek.app.data.repository.ProjectRepository
import com.shyxseek.app.rag.EmbeddingProvider
import com.shyxseek.app.rag.HashEmbeddingProvider
import com.shyxseek.app.rag.RetrievalEngine
import com.shyxseek.app.security.SecretStore
import com.shyxseek.app.settings.AppSettings
import com.shyxseek.app.tools.KnowledgeSearchTool
import com.shyxseek.app.tools.Tool
import com.shyxseek.app.tools.ToolRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun db(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "shyxseek.db"
        ).build()

    @Provides fun conversations(db: AppDatabase): ConversationDao = db.conversations()
    @Provides fun messages(db: AppDatabase): MessageDao = db.messages()
    @Provides fun knowledge(db: AppDatabase): KnowledgeDao = db.knowledge()
    @Provides fun memories(db: AppDatabase): MemoryDao = db.memories()
    @Provides fun projects(db: AppDatabase): ProjectDao = db.projects()

    @Provides
    @Singleton
    fun secret(@ApplicationContext context: Context) = SecretStore(context)

    @Provides
    @Singleton
    fun settings(
        @ApplicationContext context: Context,
        secrets: SecretStore
    ) = AppSettings(context, secrets)

    @Provides
    @Singleton
    fun http() = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    @Provides
    @Singleton
    fun embedding(): EmbeddingProvider = HashEmbeddingProvider()

    @Provides
    @Singleton
    fun retrieval(
        knowledge: KnowledgeDao,
        memory: MemoryDao,
        embeddings: EmbeddingProvider
    ) = RetrievalEngine(knowledge, memory, embeddings)

    @Provides
    @Singleton
    fun conversationRepo(
        conversations: ConversationDao,
        messages: MessageDao
    ) = ConversationRepository(conversations, messages)

    @Provides
    @Singleton
    fun knowledgeRepo(
        knowledge: KnowledgeDao,
        retrieval: RetrievalEngine
    ) = KnowledgeRepository(knowledge, retrieval)

    @Provides
    @Singleton
    fun memoryRepo(
        memory: MemoryDao,
        retrieval: RetrievalEngine
    ) = MemoryRepository(memory, retrieval)

    @Provides
    @Singleton
    fun projectRepo(projects: ProjectDao) = ProjectRepository(projects)

    @Provides
    @Singleton
    fun fake() = FakeAIProvider()

    @Provides
    @Singleton
    fun localModels(
        @ApplicationContext context: Context,
        client: OkHttpClient
    ) = LocalModelManager(context, client)

    @Provides
    @Singleton
    fun localProvider(
        models: LocalModelManager,
        settings: AppSettings
    ) = LocalAIProvider(models, settings)

    @Provides
    @Singleton
    fun gemini(
        client: OkHttpClient,
        settings: AppSettings
    ) = GeminiProvider(client, settings)

    @Provides
    @Singleton
    fun openAI(
        client: OkHttpClient,
        settings: AppSettings
    ) = OpenAICompatibleProvider(client, settings)

    @Provides
    @Singleton
    fun providers(
        fake: FakeAIProvider,
        local: LocalAIProvider,
        gemini: GeminiProvider,
        openAI: OpenAICompatibleProvider
    ) = AIProviderRegistry(fake, local, gemini, openAI)

    @Provides
    @Singleton
    fun knowledgeTool(retrieval: RetrievalEngine) =
        KnowledgeSearchTool(retrieval)

    @Provides
    @Singleton
    fun toolSet(
        knowledge: KnowledgeSearchTool
    ): Set<Tool> = setOf(knowledge)

    @Provides
    @Singleton
    fun registry(
        tools: Set<@JvmSuppressWildcards Tool>
    ) = ToolRegistry(tools)

    @Provides
    @Singleton
    fun prompts() = SystemPromptBuilder()

    @Provides
    @Singleton
    fun orchestrator(
        providers: AIProviderRegistry,
        retrieval: RetrievalEngine,
        prompts: SystemPromptBuilder,
        tools: ToolRegistry,
        settings: AppSettings
    ) = AgentOrchestrator(
        providers,
        retrieval,
        prompts,
        tools,
        settings
    )
}
