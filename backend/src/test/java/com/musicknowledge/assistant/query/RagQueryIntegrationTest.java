package com.musicknowledge.assistant.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import com.musicknowledge.assistant.knowledge.repository.KnowledgeSourceRepository;
import com.musicknowledge.assistant.knowledge.service.EmbeddingProvider;
import com.musicknowledge.assistant.query.service.AnswerGenerator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
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
      "app.embedding.provider=fake",
      "app.llm.provider=fake",
      "app.retrieval.native-vector=false",
      "app.retrieval.vector-top-k=3",
      "app.retrieval.grounding-threshold=0.75"
    })
@Transactional
class RagQueryIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private KnowledgeSourceRepository sourceRepository;

  @Autowired private DocumentChunkRepository chunkRepository;

  @Autowired private FakeAnswerGenerator fakeAnswerGenerator;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void queryReturnsGroundedAnswerAndSources() throws Exception {
    KnowledgeSource source = new KnowledgeSource();
    source.setTitle("Wikipedia: Everybody Wants to Rule the World");
    source.setSourceType("WIKIPEDIA");
    source.setUri("https://en.wikipedia.org/wiki/Everybody_Wants_to_Rule_the_World");
    source.setContent("Tears for Fears released the song in 1985.");
    KnowledgeSource savedSource = sourceRepository.save(source);

    DocumentChunk chunk = new DocumentChunk();
    chunk.setKnowledgeSource(savedSource);
    chunk.setChunkIndex(0);
    chunk.setContent(source.getContent());
    chunk.setEmbeddingStatus(DocumentChunk.EmbeddingStatus.READY);
    chunk.setEmbeddingModel("fake-embedding-v1");
    chunk.setEmbeddingJson("[1.0,0.0,0.0]");
    chunkRepository.save(chunk);

    mockMvc
        .perform(
            post("/api/v1/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"When was the song released?\"}"))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              assertThat(body).contains("The song was released in 1985.");
              assertThat(body).contains("Everybody_Wants_to_Rule_the_World");
            });
  }

  @Test
  void blankQuestionIsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"   \"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void queryReturnsGroundedNoResultWhenNoReadyChunksExist() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"What is not in the catalog?\"}"))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              assertThat(body).contains("I could not find enough grounded information");
              assertThat(body).contains("\"sources\":[]");
            });
  }

  @Test
  void queryFallsBackToKeywordSearchWhenSemanticRetrievalHasNoMatch() throws Exception {
    KnowledgeSource source = new KnowledgeSource();
    source.setTitle("Wikipedia: Electric Light Orchestra");
    source.setSourceType("WIKIPEDIA");
    source.setUri("https://example.test/electric-light-orchestra");
    source.setContent("Electric Light Orchestra combined rock music with orchestral arrangements.");
    KnowledgeSource savedSource = sourceRepository.save(source);

    DocumentChunk chunk = new DocumentChunk();
    chunk.setKnowledgeSource(savedSource);
    chunk.setChunkIndex(0);
    chunk.setContent(source.getContent());
    chunk.setEmbeddingStatus(DocumentChunk.EmbeddingStatus.READY);
    chunk.setEmbeddingModel("fake-embedding-v1");
    chunk.setEmbeddingJson("[0.0,1.0,0.0]");
    chunkRepository.save(chunk);

    mockMvc
        .perform(
            post("/api/v1/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"What is Electric Light Orchestra?\"}"))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              assertThat(body).contains("The song was released in 1985.");
              assertThat(body).contains("electric-light-orchestra");
            });
  }

  @Test
  void answerProviderFailureReturnsGroundedFallback() throws Exception {
    KnowledgeSource source = new KnowledgeSource();
    source.setTitle("Wikipedia: fallback source");
    source.setSourceType("WIKIPEDIA");
    source.setUri("https://en.wikipedia.org/wiki/Fallback");
    source.setContent("Fallback source content.");
    KnowledgeSource savedSource = sourceRepository.save(source);

    DocumentChunk chunk = new DocumentChunk();
    chunk.setKnowledgeSource(savedSource);
    chunk.setChunkIndex(0);
    chunk.setContent(source.getContent());
    chunk.setEmbeddingStatus(DocumentChunk.EmbeddingStatus.READY);
    chunk.setEmbeddingModel("fake-embedding-v1");
    chunk.setEmbeddingJson("[1.0,0.0,0.0]");
    chunkRepository.save(chunk);
    fakeAnswerGenerator.fail = true;

    mockMvc
        .perform(
            post("/api/v1/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"What is the fallback?\"}"))
        .andExpect(status().isOk())
        .andExpect(
            result ->
                assertThat(result.getResponse().getContentAsString())
                    .contains("I could not generate a grounded answer", "Fallback"));
  }

  @TestConfiguration
  static class FakeQueryConfiguration {

    @Bean
    FakeAnswerGenerator answerGenerator() {
      return new FakeAnswerGenerator();
    }

    @Bean
    EmbeddingProvider embeddingProvider() {
      return new EmbeddingProvider() {
        @Override
        public String model() {
          return "fake-embedding-v1";
        }

        @Override
        public List<Float> embed(String text) {
          return List.of(1.0f, 0.0f, 0.0f);
        }
      };
    }
  }

  static class FakeAnswerGenerator implements AnswerGenerator {

    private boolean fail;

    @Override
    public String generate(String question, List<RetrievedSource> sources) {
      if (fail) {
        throw new IllegalStateException("synthetic answer failure");
      }
      return "The song was released in 1985.";
    }
  }
}
