# RAG

v0.1 uses hybrid retrieval: local hash embedding cosine similarity + lexical term matches + pinned boost + project affinity filtering. The `EmbeddingProvider` is replaceable, so a local neural model or remote embedding API can be added later.
