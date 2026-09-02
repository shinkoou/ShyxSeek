package com.shyxseek.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shyxseek.app.domain.*

@Entity(tableName = "projects")
data class ProjectEntity(@PrimaryKey val id: Long = 0, val name: String, val description: String = "", val status: String = "active", val goal: String = "", val currentState: String = "", val nextStepsJson: String = "[]", val createdAt: Long, val updatedAt: Long, val archived: Boolean = false)

@Entity(tableName = "conversations", indices = [Index("projectId")])
data class ConversationEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val projectId: Long? = null, val createdAt: Long, val updatedAt: Long, val summary: String = "", val archived: Boolean = false)

@Entity(tableName = "messages", indices = [Index("conversationId")])
data class MessageEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val conversationId: Long, val role: MessageRole, val content: String, val timestamp: Long, val modelId: String? = null, val status: String = "complete")

@Entity(tableName = "knowledge", indices = [Index("topic"), Index("projectId")])
data class KnowledgeEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val topic: String, val subtopic: String = "", val content: String, val summary: String = "", val source: KnowledgeSource, val confidence: Double = 1.0, val importance: Int = 5, val embeddingJson: String = "[]", val tags: String = "", val projectId: Long? = null, val createdAt: Long, val updatedAt: Long, val lastUsedAt: Long? = null, val useCount: Int = 0, val isCorrected: Boolean = false, val isPinned: Boolean = false, val isArchived: Boolean = false, val supersedesId: Long? = null)

@Entity(tableName = "memories", indices = [Index("memoryType"), Index("projectId")])
data class MemoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val memoryType: MemoryType, val content: String, val summary: String = "", val embeddingJson: String = "[]", val importance: Int = 5, val projectId: Long? = null, val createdAt: Long, val updatedAt: Long, val lastAccessedAt: Long? = null, val accessCount: Int = 0, val isPinned: Boolean = false, val isSensitive: Boolean = false, val isArchived: Boolean = false)
