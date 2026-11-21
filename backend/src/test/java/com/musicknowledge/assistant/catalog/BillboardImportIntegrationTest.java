package com.musicknowledge.assistant.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musicknowledge.assistant.catalog.domain.BillboardChartEntry;
import com.musicknowledge.assistant.catalog.repository.AlbumRepository;
import com.musicknowledge.assistant.catalog.repository.ArtistRepository;
import com.musicknowledge.assistant.catalog.repository.BillboardChartEntryRepository;
import com.musicknowledge.assistant.catalog.repository.TrackRepository;
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
class BillboardImportIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private BillboardChartEntryRepository chartEntryRepository;

  @Autowired private ArtistRepository artistRepository;

  @Autowired private AlbumRepository albumRepository;

  @Autowired private TrackRepository trackRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void importsBillboardCsvAndIsIdempotent() throws Exception {
    String csv =
        """
            year,week,rank,title,artist,album
            1980,1,1,Another One Bites the Dust,Queen,The Game
            1989,52,1,Another Day in Paradise,Phil Collins,But Seriously
            """;

    mockMvc
        .perform(post("/api/v1/catalog/imports/billboard").contentType("text/csv").content(csv))
        .andExpect(status().isOk());

    mockMvc
        .perform(post("/api/v1/catalog/imports/billboard").contentType("text/csv").content(csv))
        .andExpect(status().isOk());

    assertThat(chartEntryRepository.count()).isEqualTo(2);
    assertThat(artistRepository.findAll()).extracting("name").contains("Queen", "Phil Collins");
    assertThat(albumRepository.findAll()).extracting("title").contains("The Game", "But Seriously");
    assertThat(trackRepository.findAll())
        .extracting("title")
        .contains("Another One Bites the Dust", "Another Day in Paradise");

    BillboardChartEntry entry =
        chartEntryRepository.findByYearAndWeekAndRank(1980, 1, 1).orElseThrow();
    assertThat(entry.getTrack().getTitle()).isEqualTo("Another One Bites the Dust");
  }

  @Test
  void rejectsRowsOutside1980s() throws Exception {
    String csv = "year,week,rank,title,artist\n1979,52,1,Still,Artist\n";

    mockMvc
        .perform(
            post("/api/v1/catalog/imports/billboard")
                .contentType(MediaType.parseMediaType("text/csv"))
                .content(csv))
        .andExpect(status().isBadRequest());

    assertThat(chartEntryRepository.count()).isZero();
  }
}
