package com.musicknowledge.assistant.knowledge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.embedding.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiEmbeddingProvider implements EmbeddingProvider {

    private final RestClient restClient;
    private final String model;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiEmbeddingProvider(
        @org.springframework.beans.factory.annotation.Value("${app.embedding.model:text-embedding-004}") String model,
        @org.springframework.beans.factory.annotation.Value("${app.embedding.api-key:}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com").build();
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public List<Float> embed(String text) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is required for Gemini embeddings");
        }

        String response = restClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/v1beta/models/{model}:batchEmbedContents")
                .queryParam("key", apiKey)
                .build(model))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"requests\":[{\"model\":\"models/" + escapeJson(model)
                + "\",\"content\":{\"parts\":[{\"text\":\"" + escapeJson(text)
                + "\"}]}}]}")
            .retrieve()
            .body(String.class);

        try {
            JsonNode values = objectMapper.readTree(response).path("embeddings").path(0).path("values");
            if (!values.isArray() || values.isEmpty()) {
                throw new IllegalStateException("Gemini returned no embedding");
            }
            return objectMapper.readerForListOf(Float.class).readValue(values.toString());
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Unable to parse Gemini embedding response", exception);
        }
    }

    private String escapeJson(String value) {
        try {
            String encoded = objectMapper.writeValueAsString(value);
            return encoded.substring(1, encoded.length() - 1);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to encode Gemini request", exception);
        }
    }
}