CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    knowledge_source_id BIGINT NOT NULL REFERENCES knowledge_sources(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    embedding_model VARCHAR(150),
    embedding_json TEXT,
    UNIQUE (knowledge_source_id, chunk_index)
);

CREATE INDEX idx_document_chunks_source_id ON document_chunks(knowledge_source_id);