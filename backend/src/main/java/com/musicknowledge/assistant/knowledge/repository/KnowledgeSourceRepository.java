package com.musicknowledge.assistant.knowledge.repository;

import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeSourceRepository extends JpaRepository<KnowledgeSource, Long> {}
