package com.musicknowledge.assistant.catalog.controller;

import com.musicknowledge.assistant.catalog.domain.Artist;
import com.musicknowledge.assistant.catalog.repository.ArtistRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final ArtistRepository artistRepository;

    public CatalogController(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @GetMapping("/artists")
    public ResponseEntity<List<Artist>> getArtists() {
        return ResponseEntity.ok(artistRepository.findAll());
    }
}
