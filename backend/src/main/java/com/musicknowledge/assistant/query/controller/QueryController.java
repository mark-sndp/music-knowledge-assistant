package com.musicknowledge.assistant.query.controller;

import com.musicknowledge.assistant.query.service.RagQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/query")
public class QueryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

    private final RagQueryService ragQueryService;

    public QueryController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping
    public ResponseEntity<RagQueryService.QueryResult> query(@Valid @RequestBody QueryRequest request) {
        LOGGER.info("Received query request (questionLength={})", request.question().length());
        RagQueryService.QueryResult result = ragQueryService.answer(request.question());
        LOGGER.info("Completed query request (sourceCount={})", result.sources().size());
        return ResponseEntity.ok(result);
    }

    public record QueryRequest(@NotBlank String question) {
    }
}