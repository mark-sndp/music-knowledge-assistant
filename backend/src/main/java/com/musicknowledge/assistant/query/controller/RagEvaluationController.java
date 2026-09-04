package com.musicknowledge.assistant.query.controller;

import com.musicknowledge.assistant.query.service.RagEvaluationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/evaluations/rag")
public class RagEvaluationController {

    private final RagEvaluationService evaluationService;

    public RagEvaluationController(RagEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ResponseEntity<RagEvaluationService.EvaluationReport> evaluate(
        @RequestBody List<RagEvaluationService.EvaluationCase> cases
    ) {
        return ResponseEntity.ok(evaluationService.evaluate(cases));
    }
}