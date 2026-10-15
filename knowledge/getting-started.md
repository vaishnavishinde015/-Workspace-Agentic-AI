# Agentic RAG API

This Java Spring Boot application provides an agentic chat API backed by OpenRouter and local
retrieval-augmented generation (RAG). It accepts questions at `POST /api/chat`, searches
Markdown and plain-text files under `knowledge/`, and gives relevant passages to the model
when composing an answer. The model can also call the knowledge search tool for more detail.

The app exposes the same search capability over MCP with `searchKnowledgeBase` and provides
`listKnowledgeSources` to list loaded files. Its MCP HTTP/SSE endpoint is `/sse`.

The current retriever uses simple keyword matching to keep the example dependency-light.
For production semantic search, replace it with embeddings and a vector database.
