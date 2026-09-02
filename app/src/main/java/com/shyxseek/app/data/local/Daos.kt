package com.shyxseek.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface ConversationDao {
 @Query("SELECT * FROM conversations WHERE archived=0 ORDER BY updatedAt DESC") fun observeAll(): Flow<List<ConversationEntity>>
 @Insert suspend fun insert(entity: ConversationEntity): Long
 @Update suspend fun update(entity: ConversationEntity)
}
@Dao interface MessageDao {
 @Query("SELECT * FROM messages WHERE conversationId=:conversationId ORDER BY timestamp ASC, id ASC") fun observe(conversationId:Long): Flow<List<MessageEntity>>
 @Insert suspend fun insert(entity: MessageEntity): Long
 @Query("SELECT * FROM messages WHERE conversationId=:conversationId ORDER BY timestamp ASC, id ASC") suspend fun list(conversationId:Long): List<MessageEntity>
}
@Dao interface KnowledgeDao {
 @Query("SELECT * FROM knowledge WHERE isArchived=0 ORDER BY isPinned DESC, updatedAt DESC") fun observeAll(): Flow<List<KnowledgeEntity>>
 @Query("SELECT * FROM knowledge WHERE isArchived=0") suspend fun all(): List<KnowledgeEntity>
 @Insert suspend fun insert(entity: KnowledgeEntity): Long
 @Delete suspend fun delete(entity: KnowledgeEntity)
}
@Dao interface MemoryDao {
 @Query("SELECT * FROM memories WHERE isArchived=0 ORDER BY isPinned DESC, updatedAt DESC") fun observeAll(): Flow<List<MemoryEntity>>
 @Query("SELECT * FROM memories WHERE isArchived=0") suspend fun all(): List<MemoryEntity>
 @Insert suspend fun insert(entity: MemoryEntity): Long
 @Delete suspend fun delete(entity: MemoryEntity)
}
@Dao interface ProjectDao {
 @Query("SELECT * FROM projects WHERE archived=0 ORDER BY updatedAt DESC") fun observeAll(): Flow<List<ProjectEntity>>
 @Query("SELECT * FROM projects WHERE id=:id LIMIT 1") suspend fun get(id:Long): ProjectEntity?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(entity:ProjectEntity):Long
}
