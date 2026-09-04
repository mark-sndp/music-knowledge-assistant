package com.musicknowledge.assistant.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.musicknowledge.assistant.knowledge.service.EmbeddingProvider;
import com.musicknowledge.assistant.query.service.AnswerGenerator;
import com.musicknowledge.assistant.query.service.RagQueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.embedding.provider=fake",
    "app.llm.provider=fake",
    "app.retrieval.native-vector=false"
})
class QueryProviderFailureIntegrationTest {

    @Autowired
    private RagQueryService queryService;

    @Test
    void embeddingProviderFailureReturnsControlledResult() {
        RagQueryService.QueryResult result = queryService.answer("Which source should be searched?");

        assertThat(result.answer()).isEqualTo("I could not retrieve grounded information for that question.");
        assertThat(result.sources()).isEmpty();
    }

    @TestConfiguration
    static class FailingProviderConfiguration {

        @Bean
        EmbeddingProvider embeddingProvider() {
            return new EmbeddingProvider() {
                @Override
                public String model() {
                    return "failing-provider";
                }

                @Override
                public List<Float> embed(String text) {
                    throw new IllegalStateException("synthetic embedding outage");
                }
            };
        }

        @Bean
        AnswerGenerator answerGenerator() {
            return (question, sources) -> "This answer must not be generated.";
        }
    }
}
