package com.musicknowledge.assistant.query.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RagQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagQueryService.class);

    private final JsonEmbeddingRetrievalService retrievalService;
    private final AnswerGenerator answerGenerator;

    public RagQueryService(JsonEmbeddingRetrievalService retrievalService, AnswerGenerator answerGenerator) {
        this.retrievalService = retrievalService;
        this.answerGenerator = answerGenerator;
    }

    public QueryResult answer(String question) {
        List<AnswerGenerator.RetrievedSource> sources;
        try {
            LOGGER.info("Retrieving grounded sources");
            sources = retrievalService.retrieve(question);
            LOGGER.info("Retrieved {} grounded source(s)", sources.size());
        } catch (RuntimeException exception) {
            LOGGER.warn("Grounded retrieval failed", exception);
            return new QueryResult("I could not retrieve grounded information for that question.", List.of());
        }
        String answer;
        if (sources.isEmpty()) {
            LOGGER.info("No sources met the grounding threshold");
            answer = "I could not find enough grounded information to answer that question.";
        } else {
            try {
                LOGGER.info("Generating answer from {} source(s)", sources.size());
                answer = answerGenerator.generate(question, sources);
                LOGGER.info("Generated grounded answer (answerLength={})", answer.length());
            } catch (RuntimeException exception) {
                LOGGER.warn("Grounded answer generation failed", exception);
                answer = "I could not generate a grounded answer from the retrieved sources.";
            }
        }
        return new QueryResult(answer, sources);
    }

    public record QueryResult(String answer, List<AnswerGenerator.RetrievedSource> sources) {
    }
}