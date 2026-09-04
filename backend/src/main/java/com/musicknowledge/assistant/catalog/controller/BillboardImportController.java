package com.musicknowledge.assistant.catalog.controller;

import com.musicknowledge.assistant.catalog.service.BillboardImportService;
import java.io.StringReader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/imports")
public class BillboardImportController {

    private final BillboardImportService importService;

    public BillboardImportController(BillboardImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value = "/billboard", consumes = "text/csv", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BillboardImportService.ImportReport> importBillboard(@RequestBody String csv) {
        try {
            return ResponseEntity.ok(importService.importCsv(new StringReader(csv)));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}