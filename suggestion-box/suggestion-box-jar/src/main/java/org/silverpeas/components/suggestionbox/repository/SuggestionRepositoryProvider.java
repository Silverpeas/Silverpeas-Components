package org.silverpeas.components.suggestionbox.repository;

import javax.inject.Inject;

/**
 * @author mmoquillon
 */
public class SuggestionRepositoryProvider {

  private static final SuggestionRepositoryProvider instance = new SuggestionRepositoryProvider();

  @Inject
  private SuggestionRepository repository;

  public static final SuggestionRepositoryProvider getInstance() {
    return instance;
  }

  public static final SuggestionRepository getSuggestionRepository() {
    return getInstance().repository;
  }
}
