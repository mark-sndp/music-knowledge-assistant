package com.musicknowledge.assistant.knowledge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmbeddingGenerationService {

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingProvider embeddingProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmbeddingGenerationService(
        DocumentChunkRepository chunkRepository,
        EmbeddingProvider embeddingProvider
    ) {
        this.chunkRepository = chunkRepository;
        this.embeddingProvider = embeddingProvider;
    }

    @Transactional
    public List<DocumentChunk> generateForSource(Long sourceId) {
        List<DocumentChunk> pendingChunks = chunkRepository
            .findAllByKnowledgeSourceIdOrderByChunkIndex(sourceId).stream()
            .filter(chunk -> chunk.getEmbeddingStatus() == DocumentChunk.EmbeddingStatus.PENDING
                || chunk.getEmbeddingStatus() == DocumentChunk.EmbeddingStatus.FAILED)
            .toList();

        for (DocumentChunk chunk : pendingChunks) {
            try {
                chunk.setEmbeddingJson(objectMapper.writeValueAsString(embeddingProvider.embed(chunk.getContent())));
                chunk.setEmbeddingModel(embeddingProvider.model());
                chunk.setEmbeddingStatus(DocumentChunk.EmbeddingStatus.READY);
            } catch (RuntimeException | JsonProcessingException exception) {
                chunk.setEmbeddingStatus(DocumentChunk.EmbeddingStatus.FAILED);
                chunk.setEmbeddingModel(embeddingProvider.model());
                chunk.setEmbeddingJson(null);
            }
        }
        return chunkRepository.saveAll(pendingChunks);
    }
}