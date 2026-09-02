package com.shyxseek.app
import com.shyxseek.app.rag.HashEmbeddingProvider
import kotlin.test.Test
import kotlin.test.assertEquals
class EmbeddingTest { @Test fun dimensions(){ kotlinx.coroutines.runBlocking{ assertEquals(384,HashEmbeddingProvider().embed("Vulkan shader").size) } } }
