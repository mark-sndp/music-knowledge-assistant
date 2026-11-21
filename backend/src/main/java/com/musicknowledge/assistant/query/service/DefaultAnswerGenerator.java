package com.musicknowledge.assistant.query.service;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(AnswerGenerator.class)
public class DefaultAnswerGenerator implements AnswerGenerator {

  @Override
  public String generate(String question, List<RetrievedSource> sources) {
    return sources.getFirst().content();
  }
}
