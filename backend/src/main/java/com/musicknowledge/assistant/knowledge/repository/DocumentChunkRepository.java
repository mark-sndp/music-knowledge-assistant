package com.musicknowledge.assistant.knowledge.repository;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findAllByKnowledgeSourceIdOrderByChunkIndex(Long knowledgeSourceId);

    void deleteAllByKnowledgeSourceId(Long knowledgeSourceId);
}