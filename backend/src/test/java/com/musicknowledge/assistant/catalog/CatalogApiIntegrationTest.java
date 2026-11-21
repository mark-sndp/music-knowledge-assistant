package com.musicknowledge.assistant.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musicknowledge.assistant.catalog.domain.Album;
import com.musicknowledge.assistant.catalog.domain.Artist;
import com.musicknowledge.assistant.catalog.domain.Genre;
import com.musicknowledge.assistant.catalog.domain.Track;
import com.musicknowledge.assistant.catalog.repository.AlbumRepository;
import com.musicknowledge.assistant.catalog.repository.ArtistRepository;
import com.musicknowledge.assistant.catalog.repository.GenreRepository;
import com.musicknowledge.assistant.catalog.repository.TrackRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
class CatalogApiIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Autowired private ArtistRepository artistRepository;

  @Autowired private AlbumRepository albumRepository;

  @Autowired private GenreRepository genreRepository;

  @Autowired private TrackRepository trackRepository;

  @Test
  void artistsEndpointReturnsPersistedArtists() throws Exception {
    Artist artist = new Artist();
    artist.setName("Pink Floyd");
    artist.setBiography("Progressive rock band.");
    artist.setCountry("United Kingdom");
    artist.setActiveFrom(LocalDate.of(1965, 1, 1));
    artist.setActiveTo(LocalDate.of(2014, 1, 1));
    artistRepository.save(artist);

    mockMvc
        .perform(get("/api/v1/artists").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              assertThat(body).contains("Pink Floyd");
            });
  }

  @Test
  void trackRepositoryCanPersistGenreRelation() {
    Genre genre = new Genre();
    genre.setName("Progressive Rock");
    genre.setDescription("Experimental rock");
    Genre savedGenre = genreRepository.save(genre);

    Track track = new Track();
    track.setTitle("Comfortably Numb");
    track.setDuration(383);
    track.setReleaseDate(LocalDate.of(1979, 10, 12));
    track.setDescription("A classic progressive rock anthem.");
    track.setGenres(List.of(savedGenre));

    Track result = trackRepository.save(track);

    assertThat(result.getGenres()).extracting(Genre::getName).contains("Progressive Rock");
  }

  @Test
  void albumsEndpointReturnsPersistedAlbums() throws Exception {
    Artist artist = new Artist();
    artist.setName("Radiohead");
    Artist savedArtist = artistRepository.save(artist);

    Album album = new Album();
    album.setTitle("OK Computer");
    album.setArtist(savedArtist);
    albumRepository.save(album);

    mockMvc
        .perform(get("/api/v1/albums").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            result ->
                assertThat(result.getResponse().getContentAsString()).contains("OK Computer"));
  }
}
