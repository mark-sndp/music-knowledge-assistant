package com.musicknowledge.assistant.query.service;

import java.util.List;

public interface EmbeddingQueryProvider {

  List<Float> embed(String text);
}
