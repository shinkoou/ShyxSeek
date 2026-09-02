# Architecture

UI (Compose) → ViewModel → AgentOrchestrator → AIProvider + RetrievalEngine + ToolRegistry → Repositories → Room / DataStore / Network.

The first release is package-modular rather than Gradle multi-module to keep build complexity low. Interfaces are kept around provider, embeddings and tools so those areas can evolve independently.
