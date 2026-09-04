package com.musicknowledge.assistant.query.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiAnswerGenerator implements AnswerGenerator {

    private final RestClient restClient;
    private final String model;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiAnswerGenerator(
        @org.springframework.beans.factory.annotation.Value("${app.llm.model:gemini-2.0-flash}") String model,
        @org.springframework.beans.factory.annotation.Value("${app.llm.api-key:}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com").build();
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String generate(String question, List<RetrievedSource> sources) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is required for Gemini answers");
        }

        String context = sources.stream()
            .map(source -> "Source: " + source.title() + " (" + source.uri() + ")\n" + source.content())
            .reduce((left, right) -> left + "\n\n" + right)
            .orElse("");
        String prompt = "Answer the question using only the provided sources. If the sources do not support an answer, say so. "
            + "Do not invent facts.\n\nQuestion: " + question + "\n\nSources:\n" + context;

        String response = restClient.post()
            .uri(uriBuilder -> uriBuilder.path("/v1beta/models/{model}:generateContent")
                .queryParam("key", apiKey).build(model))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}]}")
            .retrieve()
            .body(String.class);

        try {
            JsonNode text = objectMapper.readTree(response).path("candidates").path(0)
                .path("content").path("parts").path(0).path("text");
            if (!text.isTextual() || text.asText().isBlank()) {
                throw new IllegalStateException("Gemini returned no answer");
            }
            return text.asText();
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Unable to parse Gemini answer response", exception);
        }
    }

    private String escapeJson(String value) {
        try {
            String encoded = objectMapper.writeValueAsString(value);
            return encoded.substring(1, encoded.length() - 1);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to encode Gemini answer request", exception);
        }
    }
}