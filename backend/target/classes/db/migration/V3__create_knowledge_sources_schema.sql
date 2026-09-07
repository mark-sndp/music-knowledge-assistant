CREATE TABLE knowledge_sources (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(250) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    uri VARCHAR(1000) NOT NULL,
    content TEXT NOT NULL,
    authority INTEGER NOT NULL DEFAULT 1 CHECK (authority BETWEEN 1 AND 5),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_sources_source_type ON knowledge_sources(source_type);