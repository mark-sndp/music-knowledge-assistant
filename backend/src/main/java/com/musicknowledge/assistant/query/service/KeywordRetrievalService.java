package com.musicknowledge.assistant.query.service;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class KeywordRetrievalService {

  private static final Set<String> STOP_WORDS =
      Set.of(
          "a", "an", "and", "are", "does", "for", "how", "in", "is", "it", "of", "on", "the", "to",
          "was", "what", "when", "where", "which", "who", "why", "with");

  private final DocumentChunkRepository chunkRepository;

  public KeywordRetrievalService(DocumentChunkRepository chunkRepository) {
    this.chunkRepository = chunkRepository;
  }

  public List<AnswerGenerator.RetrievedSource> retrieve(String question, int topK) {
    List<String> keywords = keywords(question);
    if (keywords.isEmpty()) {
      return List.of();
    }

    int candidateLimit = Math.max(topK * 3, 10);
    Map<Long, DocumentChunk> candidates =
        keywords.stream()
            .flatMap(
                keyword ->
                    chunkRepository
                        .findKeywordCandidates(keyword, PageRequest.of(0, candidateLimit))
                        .stream())
            .collect(
                Collectors.toMap(DocumentChunk::getId, Function.identity(), (left, right) -> left));

    String normalizedQuestion = normalize(question);
    return candidates.values().stream()
        .map(chunk -> toRetrievedSource(chunk, keywords, normalizedQuestion))
        .filter(source -> source.score() > 0)
        .sorted(Comparator.comparingDouble(AnswerGenerator.RetrievedSource::score).reversed())
        .limit(topK)
        .toList();
  }

  private List<String> keywords(String question) {
    return new LinkedHashSet<>(List.of(normalize(question).split(" ")))
        .stream()
            .filter(keyword -> keyword.length() > 1)
            .filter(keyword -> !STOP_WORDS.contains(keyword))
            .toList();
  }

  private AnswerGenerator.RetrievedSource toRetrievedSource(
      DocumentChunk chunk, List<String> keywords, String normalizedQuestion) {
    String normalizedTitle = normalize(chunk.getKnowledgeSource().getTitle());
    String normalizedContent = normalize(chunk.getContent());
    long titleMatches = keywords.stream().filter(normalizedTitle::contains).count();
    long contentMatches = keywords.stream().filter(normalizedContent::contains).count();
    double titleScore = (double) titleMatches / keywords.size();
    double contentScore = (double) contentMatches / keywords.size();
    double phraseBonus =
        !normalizedQuestion.isBlank() && normalizedTitle.contains(normalizedQuestion) ? 1.0 : 0.0;
    double score = phraseBonus + titleScore * 2 + contentScore;

    return new AnswerGenerator.RetrievedSource(
        chunk.getKnowledgeSource().getTitle(),
        chunk.getKnowledgeSource().getUri(),
        chunk.getContent(),
        score);
  }

  private String normalize(String value) {
    return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
  }
}
