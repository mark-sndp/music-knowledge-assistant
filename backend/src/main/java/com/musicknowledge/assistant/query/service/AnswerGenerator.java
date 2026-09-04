package com.musicknowledge.assistant.query.service;

import java.util.List;

public interface AnswerGenerator {

    String generate(String question, List<RetrievedSource> sources);

    record RetrievedSource(String title, String uri, String content, double score) {
    }
}