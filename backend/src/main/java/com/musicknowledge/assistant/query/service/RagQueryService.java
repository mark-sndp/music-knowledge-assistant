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
        List<AnswerGenerator.RetrievedSource> sources;
        try {
            sources = retrievalService.retrieve(question);
        } catch (RuntimeException exception) {
            return new QueryResult("I could not retrieve grounded information for that question.", List.of());
        }
        String answer;
        if (sources.isEmpty()) {
            answer = "I could not find enough grounded information to answer that question.";
        } else {
            try {
                answer = answerGenerator.generate(question, sources);
            } catch (RuntimeException exception) {
                answer = "I could not generate a grounded answer from the retrieved sources.";
            }
        }
        return new QueryResult(answer, sources);
    }

    public record QueryResult(String answer, List<AnswerGenerator.RetrievedSource> sources) {
    }
}