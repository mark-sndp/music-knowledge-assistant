package com.musicknowledge.assistant.query.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "app.retrieval.native-vector",
    havingValue = "true",
    matchIfMissing = true)
public class PgVectorStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(PgVectorStore.class);

  private final JdbcTemplate jdbcTemplate;

  public PgVectorStore(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void save(Long chunkId, List<Float> embedding) {
    String vector = embedding.toString().replace(" ", "");
    jdbcTemplate.update(
        "UPDATE document_chunks SET embedding_vector = ?::vector WHERE id = ?", vector, chunkId);
  }

  public List<AnswerGenerator.RetrievedSource> retrieve(
      List<Float> queryEmbedding, int topK, double groundingThreshold) {
    String vector = queryEmbedding.toString().replace(" ", "");
    LOGGER.info(
        "Executing pgvector similarity search (dimensions={}, topK={}, groundingThreshold={})",
        queryEmbedding.size(),
        topK,
        groundingThreshold);
    List<AnswerGenerator.RetrievedSource> sources =
        jdbcTemplate.query(
            "SELECT s.title, s.uri, c.content, 1 - (c.embedding_vector <=> ?::vector) AS score "
                + "FROM document_chunks c JOIN knowledge_sources s ON s.id = c.knowledge_source_id "
                + "WHERE c.embedding_status = 'READY' AND c.embedding_vector IS NOT NULL "
                + "AND 1 - (c.embedding_vector <=> ?::vector) >= ? "
                + "ORDER BY c.embedding_vector <=> ?::vector LIMIT ?",
            (resultSet, rowNumber) ->
                new AnswerGenerator.RetrievedSource(
                    resultSet.getString("title"),
                    resultSet.getString("uri"),
                    resultSet.getString("content"),
                    resultSet.getDouble("score")),
            vector,
            vector,
            groundingThreshold,
            vector,
            topK);
    LOGGER.info("pgvector similarity search returned {} source(s)", sources.size());
    return sources;
  }
}
