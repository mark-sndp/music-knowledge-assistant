package com.musicknowledge.assistant.query.service;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.retrieval.native-vector", havingValue = "true", matchIfMissing = true)
public class PgVectorStore {

    private final JdbcTemplate jdbcTemplate;

    public PgVectorStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Long chunkId, List<Float> embedding) {
        String vector = embedding.toString().replace(" ", "");
        jdbcTemplate.update("UPDATE document_chunks SET embedding_vector = ?::vector WHERE id = ?", vector, chunkId);
    }

    public List<AnswerGenerator.RetrievedSource> retrieve(List<Float> queryEmbedding, int topK) {
        String vector = queryEmbedding.toString().replace(" ", "");
        return jdbcTemplate.query(
            "SELECT s.title, s.uri, c.content, 1 - (c.embedding_vector <=> ?::vector) AS score "
                + "FROM document_chunks c JOIN knowledge_sources s ON s.id = c.knowledge_source_id "
                + "WHERE c.embedding_status = 'READY' AND c.embedding_vector IS NOT NULL "
                + "ORDER BY c.embedding_vector <=> ?::vector LIMIT ?",
            (resultSet, rowNumber) -> new AnswerGenerator.RetrievedSource(
                resultSet.getString("title"),
                resultSet.getString("uri"),
                resultSet.getString("content"),
                resultSet.getDouble("score")
            ),
            vector, vector, topK
        );
    }
}