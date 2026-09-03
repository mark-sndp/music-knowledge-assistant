package com.musicknowledge.assistant.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.musicknowledge.assistant.knowledge.domain.KnowledgeSource;
import com.musicknowledge.assistant.knowledge.repository.KnowledgeSourceRepository;
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
class KnowledgeSourceApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private KnowledgeSourceRepository knowledgeSourceRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createSourcePersistsAndListsKnowledgeSource() throws Exception {
        mockMvc.perform(post("/api/v1/knowledge-sources")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "The Beatles biography",
                      "sourceType": "BOOK",
                      "uri": "https://example.test/beatles",
                      "content": "The Beatles were an English rock band formed in Liverpool."
                    }
                    """))
            .andExpect(status().isCreated());

        assertThat(knowledgeSourceRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/knowledge-sources").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                .contains("The Beatles biography", "BOOK"));
    }

    @Test
    void sourceRepositoryStoresAuthorityMetadata() {
        KnowledgeSource source = new KnowledgeSource();
        source.setTitle("Official artist archive");
        source.setSourceType("OFFICIAL_SITE");
        source.setUri("https://example.test/archive");
        source.setContent("Verified artist history.");
        source.setAuthority(5);

        KnowledgeSource saved = knowledgeSourceRepository.save(source);

        assertThat(saved.getAuthority()).isEqualTo(5);
    }
}
