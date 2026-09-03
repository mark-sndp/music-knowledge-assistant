package com.musicknowledge.assistant.knowledge.controller;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.service.EmbeddingGenerationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/knowledge-sources/{sourceId}/embeddings")
public class EmbeddingController {

    private final EmbeddingGenerationService embeddingGenerationService;

    public EmbeddingController(EmbeddingGenerationService embeddingGenerationService) {
        this.embeddingGenerationService = embeddingGenerationService;
    }

    @PostMapping
    public ResponseEntity<List<DocumentChunk>> generateEmbeddings(@PathVariable Long sourceId) {
        return ResponseEntity.ok(embeddingGenerationService.generateForSource(sourceId));
    }
}