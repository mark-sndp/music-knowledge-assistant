package com.musicknowledge.assistant.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.musicknowledge.assistant.knowledge.domain.DocumentChunk;
import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import com.musicknowledge.assistant.knowledge.repository.DocumentChunkRepository;
import com.musicknowledge.assistant.knowledge.repository.KnowledgeSourceRepository;
import com.musicknowledge.assistant.knowledge.service.EmbeddingProvider;
import com.musicknowledge.assistant.query.service.AnswerGenerator;
import com.musicknowledge.assistant.query.service.RagEvaluationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.embedding.provider=fake",
    "app.llm.provider=fake",
    "app.retrieval.native-vector=false",
    "app.retrieval.grounding-threshold=0.75"
})
@Transactional
class RagEvaluationIntegrationTest {

    @Autowired
    private KnowledgeSourceRepository sourceRepository;

    @Autowired
    private DocumentChunkRepository chunkRepository;

    @Autowired
    private RagEvaluationService evaluationService;

    @Test
    void evaluatesAnswerAndCitationAgainstExpectedEvidence() {
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

        RagEvaluationService.EvaluationReport report = evaluationService.evaluate(List.of(
            new RagEvaluationService.EvaluationCase(
                "When was the song released?",
                "1985",
                source.getUri()
            )
        ));

        assertThat(report.totalCases()).isEqualTo(1);
        assertThat(report.passedCases()).isEqualTo(1);
        assertThat(report.answerPassRate()).isEqualTo(1.0);
        assertThat(report.citationPassRate()).isEqualTo(1.0);
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
