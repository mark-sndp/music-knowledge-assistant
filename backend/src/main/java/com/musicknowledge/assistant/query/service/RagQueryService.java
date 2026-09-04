package com.musicknowledge.assistant.query.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RagQueryService {

    private final JsonEmbeddingRetrievalService retrievalService;
    private final AnswerGenerator answerGenerator;

    public RagQueryService(JsonEmbeddingRetrievalService retrievalService, AnswerGenerator answerGenerator) {
        this.retrievalService = retrievalService;
        this.answerGenerator = answerGenerator;
    }

    public QueryResult answer(String question) {
        List<AnswerGenerator.RetrievedSource> sources = retrievalService.retrieve(question);
        String answer = sources.isEmpty()
            ? "I could not find enough grounded information to answer that question."
            : answerGenerator.generate(question, sources);
        return new QueryResult(answer, sources);
    }

    public record QueryResult(String answer, List<AnswerGenerator.RetrievedSource> sources) {
    }
}