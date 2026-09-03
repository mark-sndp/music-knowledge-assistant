# ADR-002: Use a modular monolith instead of separate services

## Context

This project is a portfolio-quality assistant but not a large enterprise system. The technical requirements are significant, but the architecture should avoid unnecessary service sprawl.

## Decision

Implement a modular monolith with package-by-feature boundaries and clear interfaces.

## Alternatives

- Microservices for each domain area.
- A single giant package with little structure.

## Trade-offs

- Pros: simpler deployment, easier local development, lower operational overhead, maintainable boundaries.
- Cons: scaling beyond a certain point may eventually require decomposition.

## Consequences

- The application stays easy to run with Docker Compose.
- Domain responsibilities remain clear despite being in one codebase.
- The design still supports later extraction if needed.
