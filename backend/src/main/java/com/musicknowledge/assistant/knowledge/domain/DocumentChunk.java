package com.musicknowledge.assistant.knowledge.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

  public enum EmbeddingStatus {
    PENDING,
    READY,
    FAILED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "knowledge_source_id", nullable = false)
  @JsonIgnore
  private KnowledgeSource knowledgeSource;

  @Column(name = "chunk_index", nullable = false)
  private Integer chunkIndex;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "embedding_status", nullable = false, length = 30)
  private EmbeddingStatus embeddingStatus = EmbeddingStatus.PENDING;

  @Column(name = "embedding_model", length = 150)
  private String embeddingModel;

  @Column(name = "embedding_json", columnDefinition = "TEXT")
  private String embeddingJson;

  public Long getId() {
    return id;
  }

  public KnowledgeSource getKnowledgeSource() {
    return knowledgeSource;
  }

  public void setKnowledgeSource(KnowledgeSource knowledgeSource) {
    this.knowledgeSource = knowledgeSource;
  }

  public Integer getChunkIndex() {
    return chunkIndex;
  }

  public void setChunkIndex(Integer chunkIndex) {
    this.chunkIndex = chunkIndex;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public EmbeddingStatus getEmbeddingStatus() {
    return embeddingStatus;
  }

  public void setEmbeddingStatus(EmbeddingStatus embeddingStatus) {
    this.embeddingStatus = embeddingStatus;
  }

  public String getEmbeddingModel() {
    return embeddingModel;
  }

  public void setEmbeddingModel(String embeddingModel) {
    this.embeddingModel = embeddingModel;
  }

  public String getEmbeddingJson() {
    return embeddingJson;
  }

  public void setEmbeddingJson(String embeddingJson) {
    this.embeddingJson = embeddingJson;
  }
}
