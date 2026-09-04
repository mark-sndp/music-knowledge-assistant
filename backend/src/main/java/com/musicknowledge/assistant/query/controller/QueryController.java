package com.musicknowledge.assistant.query.controller;

import com.musicknowledge.assistant.query.service.RagQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/query")
public class QueryController {

    private final RagQueryService ragQueryService;

    public QueryController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping
    public ResponseEntity<RagQueryService.QueryResult> query(@Valid @RequestBody QueryRequest request) {
        return ResponseEntity.ok(ragQueryService.answer(request.question()));
    }

    public record QueryRequest(@NotBlank String question) {
    }
}