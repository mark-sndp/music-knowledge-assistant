# Music Knowledge Assistant

A production-oriented music knowledge assistant built with Java, Spring Boot, PostgreSQL, pgvector, and a retrieval-augmented generation (RAG) layer.

## Overview

The application follows a modular monolith architecture to keep the implementation maintainable while still supporting:

- structured music metadata in PostgreSQL
- semantic retrieval via pgvector
- LLM synthesis and explanation
- source-grounded answers
- observability, testing, and deployment support

## Current milestone

This repository is currently in Milestone 3: knowledge-source ingestion foundation.

Completed stages include the repository/tooling foundation and the music catalog persistence and read API. The current stage stores authoritative knowledge sources so later chunking, embedding, and retrieval stages have a stable input boundary.

## Local setup

1. Copy `.env.example` to `.env` and adjust values if needed.
2. Start the local stack:

```bash
docker compose up --build -d
```

3. Confirm the application is healthy:

```bash
curl http://localhost:8080/actuator/health
```

## Technology stack

- Java 21
- Spring Boot 4.x
- Spring Web
- Spring Data JPA
- Flyway
- PostgreSQL + pgvector
- Maven
- Docker + Docker Compose
- GitHub Actions

## Repository structure

```text
music-knowledge-assistant/
├── backend/
├── data/
├── docs/
├── infrastructure/
├── .github/
├── .env.example
├── docker-compose.yml
├── README.md
└── LICENSE
```

## Notes

- Keycloak authentication is intentionally deferred from the MVP.
- Frontend scaffolding is deferred until the RAG/UI milestones are reached.
- Google AI Studio Gemini is the intended provider for the first production-ready integration.
