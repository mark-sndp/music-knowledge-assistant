# Overview

This project implements a modular monolith for a music knowledge assistant. It combines structured relational music data with semantic retrieval over embeddings and an LLM for final synthesis.

## Goals

- Ground answers in authoritative metadata and knowledge documents.
- Prefer the right retrieval mechanism for each type of question.
- Keep the AI provider interchangeable behind well-defined interfaces.
- Maintain observability, evaluation, and testability from the start.

## Key design choices

- PostgreSQL stores the music catalog and structured facts.
- pgvector stores embeddings and knowledge chunks. Retrieval uses vector similarity first and title/chunk keyword matching as a fallback when no semantic result qualifies.
- The LLM is used for synthesis, explanation, and generation, not direct database access.
- Retrieval is hybrid: SQL + vector + reranking.
- The system is intentionally modular rather than split into entity microservices.

## Architecture diagram

```mermaid
flowchart TB
	UI[Static frontend\nNginx :4173]
	API[Spring Boot API\nmodular monolith :8080]
	CATALOG[Catalog module\nJPA + import service]
	KNOWLEDGE[Knowledge module\nsources + chunks + embeddings]
	QUERY[Query module\nretrieval + RAG + evaluation]
	LOGGING[Layer-specific INFO logs]
	POSTGRES[(PostgreSQL + pgvector)]
	GEMINI[Gemini provider interfaces]
	FLYWAY[Flyway migrations]

	UI --> API
	API --> CATALOG
	API --> KNOWLEDGE
	API --> QUERY
	API --> LOGGING
	CATALOG --> POSTGRES
	KNOWLEDGE --> POSTGRES
	QUERY --> POSTGRES
	KNOWLEDGE --> GEMINI
	QUERY --> GEMINI
	FLYWAY --> POSTGRES
```

## Request flow diagram

```mermaid
sequenceDiagram
	participant Browser as Browser UI
	participant Controller as QueryController
	participant Service as RagQueryService
	participant Embedder as EmbeddingProvider
	participant Vector as PgVectorStore
	participant Database as PostgreSQL
	participant Generator as AnswerGenerator

	Browser->>Controller: POST /api/v1/query
	Controller->>Service: validated question
	Service->>Embedder: embed(question)
	Embedder-->>Service: query vector
	Service->>Vector: retrieve(vector, topK, threshold)
	Vector->>Database: pgvector cosine-distance query
	Database-->>Vector: ranked chunks and source URIs
	Vector-->>Service: grounded sources
	Service->>Generator: generate(question, sources)
	Generator-->>Service: grounded answer
	Service-->>Controller: answer and citations
	Controller-->>Browser: JSON response
```
