package com.shyxseek.app.data.local
import androidx.room.TypeConverter
import com.shyxseek.app.domain.*
class Converters {
 @TypeConverter fun role(v: MessageRole)=v.name
 @TypeConverter fun role(v:String)=MessageRole.valueOf(v)
 @TypeConverter fun memory(v:MemoryType)=v.name
 @TypeConverter fun memory(v:String)=MemoryType.valueOf(v)
 @TypeConverter fun source(v:KnowledgeSource)=v.name
 @TypeConverter fun source(v:String)=KnowledgeSource.valueOf(v)
}
