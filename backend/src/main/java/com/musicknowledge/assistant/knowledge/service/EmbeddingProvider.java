package com.musicknowledge.assistant.knowledge.service;

import java.util.List;

public interface EmbeddingProvider {

    String model();

    List<Float> embed(String text);
}