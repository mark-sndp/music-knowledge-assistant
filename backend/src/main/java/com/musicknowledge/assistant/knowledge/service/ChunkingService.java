package com.musicknowledge.assistant.knowledge.service;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import com.musicknowledge.assistant.knowledge.repository.KnowledgeSourceRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChunkingService {

  private final KnowledgeSourceRepository sourceRepository;
  private final DocumentChunkRepository chunkRepository;
  private final int chunkSize;
  private final int chunkOverlap;

  public ChunkingService(
      KnowledgeSourceRepository sourceRepository,
      DocumentChunkRepository chunkRepository,
      @Value("${app.chunking.size:800}") int chunkSize,
      @Value("${app.chunking.overlap:120}") int chunkOverlap) {
    if (chunkSize <= 0 || chunkOverlap < 0 || chunkOverlap >= chunkSize) {
      throw new IllegalArgumentException(
          "Chunk overlap must be non-negative and smaller than chunk size");
    }
    this.sourceRepository = sourceRepository;
    this.chunkRepository = chunkRepository;
    this.chunkSize = chunkSize;
    this.chunkOverlap = chunkOverlap;
  }

  @Transactional
  public List<DocumentChunk> prepareChunks(Long sourceId) {
    KnowledgeSource source =
        sourceRepository
            .findById(sourceId)
            .orElseThrow(
                () -> new IllegalArgumentException("Knowledge source not found: " + sourceId));

    chunkRepository.deleteAllByKnowledgeSourceId(sourceId);
    chunkRepository.flush();
    String content = source.getContent() == null ? "" : source.getContent().trim();
    if (content.isEmpty()) {
      return List.of();
    }

    int step = chunkSize - chunkOverlap;
    List<DocumentChunk> chunks = new ArrayList<>();
    for (int start = 0, index = 0; start < content.length(); start += step, index++) {
      int end = Math.min(start + chunkSize, content.length());
      DocumentChunk chunk = new DocumentChunk();
      chunk.setKnowledgeSource(source);
      chunk.setChunkIndex(index);
      chunk.setContent(content.substring(start, end));
      chunks.add(chunk);
      if (end == content.length()) {
        break;
      }
    }
    return chunkRepository.saveAll(chunks);
  }
}
