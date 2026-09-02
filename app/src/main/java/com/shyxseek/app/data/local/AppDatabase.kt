package com.shyxseek.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities=[ProjectEntity::class,ConversationEntity::class,MessageEntity::class,KnowledgeEntity::class,MemoryEntity::class], version=1, exportSchema=true)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
 abstract fun conversations():ConversationDao
 abstract fun messages():MessageDao
 abstract fun knowledge():KnowledgeDao
 abstract fun memories():MemoryDao
 abstract fun projects():ProjectDao
}
