package com.shyxseek.app.rag

interface EmbeddingProvider { val dimensions:Int; suspend fun embed(text:String):FloatArray }

class HashEmbeddingProvider(override val dimensions:Int=384):EmbeddingProvider {
 override suspend fun embed(text:String):FloatArray {
   val v=FloatArray(dimensions)
   Regex("""[\p{L}\p{N}_]+""", RegexOption.IGNORE_CASE).findAll(text.lowercase()).forEach { m ->
     val h=m.value.hashCode(); val i=(h and Int.MAX_VALUE)%dimensions; v[i]+=if(h and 1==0) 1f else -1f
   }
   var n=0.0; v.forEach{ n += it*it }; val d=kotlin.math.sqrt(n).toFloat().coerceAtLeast(1e-6f)
   for(i in v.indices) v[i]/=d
   return v
 }
}
fun cosine(a:FloatArray,b:FloatArray):Double { val n=minOf(a.size,b.size); var s=0.0; for(i in 0 until n) s+=a[i]*b[i]; return s }
