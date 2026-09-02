package com.shyxseek.app.rag

import com.shyxseek.app.data.local.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RetrievalEngine(private val knowledge:KnowledgeDao, private val memory:MemoryDao, private val embeddings:EmbeddingProvider){
 data class Hit(val text:String,val score:Double,val type:String)
 suspend fun search(query:String,projectId:Long?,limit:Int=8):List<Hit>{
   val q=embeddings.embed(query); val terms=query.lowercase().split(Regex("""\s+""")).filter{it.length>2}.toSet()
   val kh=knowledge.all().filter{projectId==null||it.projectId==null||it.projectId==projectId}.map { e ->
     val ev=decode(e.embeddingJson); val semantic=if(ev.isEmpty())0.0 else cosine(q,ev); val lexical=terms.count{e.content.lowercase().contains(it)}*0.08; Hit(e.content,semantic+lexical+(if(e.isPinned)0.2 else 0.0),"knowledge")
   }
   val mh=memory.all().filter{projectId==null||it.projectId==null||it.projectId==projectId}.map { e ->
     val ev=decode(e.embeddingJson); val semantic=if(ev.isEmpty())0.0 else cosine(q,ev); val lexical=terms.count{e.content.lowercase().contains(it)}*0.08; Hit(e.content,semantic+lexical+(if(e.isPinned)0.2 else 0.0),"memory")
   }
   return (kh+mh).sortedByDescending{it.score}.take(limit).filter{it.score>0.05}
 }
 suspend fun encode(text:String)=Json.encodeToString(embeddings.embed(text).toList())
 private fun decode(s:String):FloatArray=runCatching{Json.decodeFromString<List<Float>>(s).toFloatArray()}.getOrDefault(floatArrayOf())
}
