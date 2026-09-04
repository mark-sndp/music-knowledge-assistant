package com.musicknowledge.assistant.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import com.musicknowledge.assistant.knowledge.repository.KnowledgeSourceRepository;
import com.musicknowledge.assistant.knowledge.service.EmbeddingProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.embedding.provider=fake",
    "app.retrieval.native-vector=false"
})
@Transactional
class EmbeddingGenerationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private KnowledgeSourceRepository sourceRepository;

    @Autowired
    private DocumentChunkRepository chunkRepository;

    @Autowired
    private FakeEmbeddingProvider fakeEmbeddingProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        fakeEmbeddingProvider.fail = false;
    }

    @Test
    void generateEmbeddingsMarksPendingChunksReady() throws Exception {
        KnowledgeSource source = new KnowledgeSource();
        source.setTitle("Embedding fixture");
        source.setSourceType("ARTICLE");
        source.setUri("https://example.test/embedding");
        source.setContent("This source has content for embedding.");
        KnowledgeSource savedSource = sourceRepository.save(source);

        DocumentChunk chunk = new DocumentChunk();
        chunk.setKnowledgeSource(savedSource);
        chunk.setChunkIndex(0);
        chunk.setContent("This source has content for embedding.");
        chunkRepository.save(chunk);

        mockMvc.perform(post("/api/v1/knowledge-sources/" + savedSource.getId() + "/embeddings"))
            .andExpect(status().isOk());

        DocumentChunk result = chunkRepository.findAllByKnowledgeSourceIdOrderByChunkIndex(savedSource.getId())
            .getFirst();
        assertThat(result.getEmbeddingStatus()).isEqualTo(DocumentChunk.EmbeddingStatus.READY);
        assertThat(result.getEmbeddingModel()).isEqualTo("fake-embedding-v1");
        assertThat(result.getEmbeddingJson()).isEqualTo("[0.1,0.2,0.3]");
    }

    @Test
    void providerFailureMarksChunkFailedForRetry() throws Exception {
        KnowledgeSource source = new KnowledgeSource();
        source.setTitle("Failed embedding fixture");
        source.setSourceType("ARTICLE");
        source.setUri("https://example.test/failure");
        source.setContent("Content that will fail embedding.");
        KnowledgeSource savedSource = sourceRepository.save(source);

        DocumentChunk chunk = new DocumentChunk();
        chunk.setKnowledgeSource(savedSource);
        chunk.setChunkIndex(0);
        chunk.setContent(source.getContent());
        chunkRepository.save(chunk);
        fakeEmbeddingProvider.fail = true;

        mockMvc.perform(post("/api/v1/knowledge-sources/" + savedSource.getId() + "/embeddings"))
            .andExpect(status().isOk());

        DocumentChunk result = chunkRepository.findAllByKnowledgeSourceIdOrderByChunkIndex(savedSource.getId())
            .getFirst();
        assertThat(result.getEmbeddingStatus()).isEqualTo(DocumentChunk.EmbeddingStatus.FAILED);
        assertThat(result.getEmbeddingModel()).isEqualTo("fake-embedding-v1");
        assertThat(result.getEmbeddingJson()).isNull();
    }

    @TestConfiguration
    static class FakeEmbeddingConfiguration {

        @Bean
        FakeEmbeddingProvider embeddingProvider() {
            return new FakeEmbeddingProvider();
        }
    }

    static class FakeEmbeddingProvider implements EmbeddingProvider {

        private boolean fail;

        @Override
        public String model() {
            return "fake-embedding-v1";
        }

        @Override
        public List<Float> embed(String text) {
            if (fail) {
                throw new IllegalStateException("synthetic provider failure");
            }
            return List.of(0.1f, 0.2f, 0.3f);
        }
    }
}
