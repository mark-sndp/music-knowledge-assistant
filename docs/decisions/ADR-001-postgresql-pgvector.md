# ADR-001: PostgreSQL with pgvector for relational and vector search

## Context

The application needs both structured music metadata and semantic retrieval for embeddings. A single relational database with vector support can simplify the data model and reduce operational complexity.

## Decision

Use PostgreSQL with the pgvector extension for both the catalog and vector storage.

## Alternatives

- Use PostgreSQL for relational data and a separate vector database such as Pinecone or Qdrant.
- Use a generic document store for semantic retrieval.

## Trade-offs

- Pros: fewer moving parts, simpler operations, unified data model, easier joins and hybrid retrieval.
- Cons: vector features are not as flexible as a dedicated vector-native database at large scale.

## Consequences

- The application can support hybrid retrieval with a single primary database.
- Operations stay simpler for an MVP and modular monolith architecture.
