package com.musicknowledge.assistant.catalog.controller;

import com.musicknowledge.assistant.catalog.domain.Album;
import com.musicknowledge.assistant.catalog.domain.Artist;
import com.musicknowledge.assistant.catalog.domain.Genre;
import com.musicknowledge.assistant.catalog.domain.Track;
import com.musicknowledge.assistant.catalog.repository.AlbumRepository;
import com.musicknowledge.assistant.catalog.repository.ArtistRepository;
import com.musicknowledge.assistant.catalog.repository.GenreRepository;
import com.musicknowledge.assistant.catalog.repository.TrackRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

  private final ArtistRepository artistRepository;
  private final AlbumRepository albumRepository;
  private final GenreRepository genreRepository;
  private final TrackRepository trackRepository;

  public CatalogController(
      ArtistRepository artistRepository,
      AlbumRepository albumRepository,
      GenreRepository genreRepository,
      TrackRepository trackRepository) {
    this.artistRepository = artistRepository;
    this.albumRepository = albumRepository;
    this.genreRepository = genreRepository;
    this.trackRepository = trackRepository;
  }

  @GetMapping("/artists")
  public ResponseEntity<List<Artist>> getArtists() {
    return ResponseEntity.ok(artistRepository.findAll());
  }

  @GetMapping("/albums")
  public ResponseEntity<List<Album>> getAlbums() {
    return ResponseEntity.ok(albumRepository.findAll());
  }

  @GetMapping("/genres")
  public ResponseEntity<List<Genre>> getGenres() {
    return ResponseEntity.ok(genreRepository.findAll());
  }

  @GetMapping("/tracks")
  public ResponseEntity<List<Track>> getTracks() {
    return ResponseEntity.ok(trackRepository.findAll());
  }
}
