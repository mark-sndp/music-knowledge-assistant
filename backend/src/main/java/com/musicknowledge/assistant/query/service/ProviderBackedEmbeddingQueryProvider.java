package com.musicknowledge.assistant.query.service;

import com.musicknowledge.assistant.knowledge.service.EmbeddingProvider;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProviderBackedEmbeddingQueryProvider implements EmbeddingQueryProvider {

    private final EmbeddingProvider embeddingProvider;

    public ProviderBackedEmbeddingQueryProvider(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    @Override
    public List<Float> embed(String text) {
        return embeddingProvider.embed(text);
    }
}