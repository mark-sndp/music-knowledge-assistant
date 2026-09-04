package com.musicknowledge.assistant.query.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JsonEmbeddingRetrievalService {

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingQueryProvider queryProvider;
    private final PgVectorStore pgVectorStore;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int topK;
    private final double groundingThreshold;

    public JsonEmbeddingRetrievalService(
        DocumentChunkRepository chunkRepository,
        EmbeddingQueryProvider queryProvider,
        org.springframework.beans.factory.ObjectProvider<PgVectorStore> pgVectorStoreProvider,
        @Value("${app.retrieval.vector-top-k:10}") int topK,
        @Value("${app.retrieval.grounding-threshold:0.65}") double groundingThreshold
    ) {
        this.chunkRepository = chunkRepository;
        this.queryProvider = queryProvider;
        this.pgVectorStore = pgVectorStoreProvider.getIfAvailable();
        this.topK = topK;
        this.groundingThreshold = groundingThreshold;
    }

    public List<AnswerGenerator.RetrievedSource> retrieve(String question) {
        List<Float> queryEmbedding = queryProvider.embed(question);
        if (pgVectorStore != null) {
            return pgVectorStore.retrieve(queryEmbedding, topK, groundingThreshold);
        }
        return chunkRepository.findAll().stream()
            .filter(chunk -> chunk.getEmbeddingStatus() == DocumentChunk.EmbeddingStatus.READY)
            .map(chunk -> toRetrievedSource(chunk, queryEmbedding))
            .filter(source -> source.score() >= groundingThreshold)
            .sorted(Comparator.comparingDouble(AnswerGenerator.RetrievedSource::score).reversed())
            .limit(topK)
            .toList();
    }

    private AnswerGenerator.RetrievedSource toRetrievedSource(DocumentChunk chunk, List<Float> queryEmbedding) {
        try {
            List<Float> embedding = objectMapper.readerForListOf(Float.class).readValue(chunk.getEmbeddingJson());
            double score = cosineSimilarity(queryEmbedding, embedding);
            return new AnswerGenerator.RetrievedSource(
                chunk.getKnowledgeSource().getTitle(),
                chunk.getKnowledgeSource().getUri(),
                chunk.getContent(),
                score
            );
        } catch (Exception exception) {
            return new AnswerGenerator.RetrievedSource(
                chunk.getKnowledgeSource().getTitle(),
                chunk.getKnowledgeSource().getUri(),
                chunk.getContent(),
                0
            );
        }
    }

    private double cosineSimilarity(List<Float> left, List<Float> right) {
        if (left.size() != right.size() || left.isEmpty()) {
            return 0;
        }
        double dot = 0;
        double leftMagnitude = 0;
        double rightMagnitude = 0;
        for (int index = 0; index < left.size(); index++) {
            double leftValue = left.get(index);
            double rightValue = right.get(index);
            dot += leftValue * rightValue;
            leftMagnitude += leftValue * leftValue;
            rightMagnitude += rightValue * rightValue;
        }
        return leftMagnitude == 0 || rightMagnitude == 0
            ? 0
            : dot / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
    }
}