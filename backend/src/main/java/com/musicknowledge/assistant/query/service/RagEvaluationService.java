package com.musicknowledge.assistant.query.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RagEvaluationService {

    private final RagQueryService queryService;

    public RagEvaluationService(RagQueryService queryService) {
        this.queryService = queryService;
    }

    public EvaluationReport evaluate(List<EvaluationCase> cases) {
        List<EvaluationResult> results = cases.stream().map(this::evaluateCase).toList();
        long answerPasses = results.stream().filter(EvaluationResult::answerPassed).count();
        long citationPasses = results.stream().filter(EvaluationResult::citationPassed).count();
        long completePasses = results.stream()
            .filter(result -> result.answerPassed() && result.citationPassed())
            .count();
        int total = results.size();
        return new EvaluationReport(
            total,
            (int) completePasses,
            rate(answerPasses, total),
            rate(citationPasses, total),
            results
        );
    }

    private EvaluationResult evaluateCase(EvaluationCase evaluationCase) {
        RagQueryService.QueryResult result = queryService.answer(evaluationCase.question());
        boolean answerPassed = result.answer().toLowerCase().contains(evaluationCase.expectedAnswerContains().toLowerCase());
        boolean citationPassed = result.sources().stream()
            .anyMatch(source -> source.uri().equals(evaluationCase.expectedSourceUri()));
        return new EvaluationResult(evaluationCase.question(), answerPassed, citationPassed, result.answer());
    }

    private double rate(long passes, int total) {
        return total == 0 ? 0.0 : (double) passes / total;
    }

    public record EvaluationCase(String question, String expectedAnswerContains, String expectedSourceUri) {
    }

    public record EvaluationResult(String question, boolean answerPassed, boolean citationPassed, String answer) {
    }

    public record EvaluationReport(
        int totalCases,
        int passedCases,
        double answerPassRate,
        double citationPassRate,
        List<EvaluationResult> results
    ) {
    }
}