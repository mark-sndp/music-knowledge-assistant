package com.musicknowledge.assistant.knowledge.controller;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import com.musicknowledge.assistant.knowledge.service.ChunkingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/knowledge-sources/{sourceId}/chunks")
public class DocumentChunkController {

    private final ChunkingService chunkingService;
    private final DocumentChunkRepository chunkRepository;

    public DocumentChunkController(ChunkingService chunkingService, DocumentChunkRepository chunkRepository) {
        this.chunkingService = chunkingService;
        this.chunkRepository = chunkRepository;
    }

    @PostMapping
    public ResponseEntity<List<DocumentChunk>> prepareChunks(@PathVariable Long sourceId) {
        return ResponseEntity.status(201).body(chunkingService.prepareChunks(sourceId));
    }

    @GetMapping
    public ResponseEntity<List<DocumentChunk>> getChunks(@PathVariable Long sourceId) {
        return ResponseEntity.ok(chunkRepository.findAllByKnowledgeSourceIdOrderByChunkIndex(sourceId));
    }
}