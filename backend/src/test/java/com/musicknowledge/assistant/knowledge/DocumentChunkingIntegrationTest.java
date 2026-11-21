package com.musicknowledge.assistant.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import com.musicknowledge.assistant.knowledge.repository.KnowledgeSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "app.chunking.size=20",
      "app.chunking.overlap=5"
    })
@Transactional
class DocumentChunkingIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private KnowledgeSourceRepository sourceRepository;

  @Autowired private DocumentChunkRepository chunkRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void prepareChunksPersistsOrderedOverlappingPendingChunks() throws Exception {
    KnowledgeSource source = new KnowledgeSource();
    source.setTitle("Short history");
    source.setSourceType("ARTICLE");
    source.setUri("https://example.test/history");
    source.setContent("012345678901234567890123456789");
    KnowledgeSource savedSource = sourceRepository.save(source);

    mockMvc
        .perform(post("/api/v1/knowledge-sources/" + savedSource.getId() + "/chunks"))
        .andExpect(status().isCreated());

    assertThat(chunkRepository.findAllByKnowledgeSourceIdOrderByChunkIndex(savedSource.getId()))
        .extracting(DocumentChunk::getContent)
        .containsExactly("01234567890123456789", "567890123456789");

    assertThat(chunkRepository.findAll())
        .allMatch(
            chunk ->
                chunk.getEmbeddingStatus() == DocumentChunk.EmbeddingStatus.PENDING
                    && chunk.getEmbeddingModel() == null);
  }

  @Test
  void chunkingDoesNotCreateEmptyChunks() {
    KnowledgeSource source = new KnowledgeSource();
    source.setTitle("Empty source");
    source.setSourceType("NOTE");
    source.setUri("https://example.test/empty");
    source.setContent("   ");
    KnowledgeSource savedSource = sourceRepository.save(source);

    assertThat(chunkRepository.findAllByKnowledgeSourceIdOrderByChunkIndex(savedSource.getId()))
        .isEmpty();
  }
}
