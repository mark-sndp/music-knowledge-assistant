package com.musicknowledge.assistant.knowledge.repository;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

  List<DocumentChunk> findAllByKnowledgeSourceIdOrderByChunkIndex(Long knowledgeSourceId);

  @Query(
      """
        SELECT c FROM DocumentChunk c JOIN FETCH c.knowledgeSource s
        WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
  List<DocumentChunk> findKeywordCandidates(@Param("keyword") String keyword, Pageable pageable);

  void deleteAllByKnowledgeSourceId(Long knowledgeSourceId);
}
