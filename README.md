# Agentic RAG API (Java)

A small Spring Boot REST API that calls OpenRouter, retrieves relevant passages from local
Markdown/text files, and exposes the same knowledge tools over MCP.

## Requirements

- Java 25+
- Maven 3.9+
- An OpenRouter API key

## Run

Put your rotated OpenRouter key in the root `.env` file:

```properties
OPENROUTER_API_KEY=your-new-openrouter-key
PORT=18080
```

The `.env` file is ignored by Git. Do not commit it or reuse a key that has been exposed.
From PowerShell, start the app from the project root:

```powershell
.\run.bat
```

The launcher prints the app URL in the console. It binds to `127.0.0.1` on port `18080`
with the settings above (default `8080`); if that port is already in use, it selects and
prints the next available port.
Add `.md` or `.txt` files under `knowledge/`; they are loaded at startup. You can also set
`KNOWLEDGE_DIRECTORY` to use another directory and `OPENROUTER_MODEL` to choose a different
OpenRouter model.

## Web interface

Open `http://localhost:18080` (or the configured server port) for the chat interface.
It is served by Spring Boot and sends questions to the same `/api/chat` endpoint.

## REST chat

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/chat `
  -ContentType "application/json" `
  -Body '{"question":"What does this project do?"}'
```

The model can call `search_knowledge_base` and gets up to three matching document chunks
per search. The current retriever is keyword-based; use an embedding model and vector store
for semantic retrieval in production. Each OpenRouter request is capped at 1,000 generated
tokens to avoid requesting more output than a low-credit key can afford.

## MCP tools

The Spring AI MCP server is available over HTTP/SSE at `http://localhost:8080/sse` with
messages at `http://localhost:8080/mcp/message`. It exposes:

- `searchKnowledgeBase(query)`
- `listKnowledgeSources()`

The HTTP MCP transport does not provide authentication by itself. Keep this starter on
localhost; before exposing it to a network, add authentication and authorization at the
application or reverse-proxy layer.
