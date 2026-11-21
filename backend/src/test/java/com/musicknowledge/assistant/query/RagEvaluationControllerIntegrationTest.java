package com.musicknowledge.assistant.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musicknowledge.assistant.knowledge.service.EmbeddingProvider;
import com.musicknowledge.assistant.query.service.AnswerGenerator;
import java.util.List;
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
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "app.embedding.provider=fake",
      "app.llm.provider=fake",
      "app.retrieval.native-vector=false"
    })
class RagEvaluationControllerIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Test
  void evaluationEndpointReturnsPassRates() throws Exception {
    MockMvc configuredMockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    configuredMockMvc
        .perform(
            post("/api/v1/evaluations/rag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    [{
                      "question": "What year?",
                      "expectedAnswerContains": "1985",
                      "expectedSourceUri": "https://example.test/source"
                    }]
                    """))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              assertThat(body)
                  .contains("\"totalCases\":1", "\"passedCases\":0", "\"answerPassRate\":0.0");
            });
  }

  @TestConfiguration
  static class FakeEvaluationConfiguration {

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

    @Bean
    AnswerGenerator answerGenerator() {
      return (question, sources) -> "The song was released in 1985.";
    }
  }
}
