package com.musicknowledge.assistant.knowledge.controller;

import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import com.musicknowledge.assistant.knowledge.repository.KnowledgeSourceRepository;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/knowledge-sources")
public class KnowledgeSourceController {

  private final KnowledgeSourceRepository repository;

  public KnowledgeSourceController(KnowledgeSourceRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public ResponseEntity<List<KnowledgeSource>> getSources() {
    return ResponseEntity.ok(repository.findAll());
  }

  @PostMapping
  public ResponseEntity<KnowledgeSource> createSource(@Valid @RequestBody KnowledgeSource source) {
    if (source.getAuthority() == null) {
      source.setAuthority(1);
    }
    if (source.getCreatedAt() == null) {
      source.setCreatedAt(OffsetDateTime.now());
    }
    return ResponseEntity.status(201).body(repository.save(source));
  }
}
