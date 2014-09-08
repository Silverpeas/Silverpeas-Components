package org.silverpeas.components.quickinfo.repository;

import javax.inject.Inject;

public class NewsRepositoryProvider {
  
  private static final NewsRepositoryProvider instance = new NewsRepositoryProvider();

  @Inject
  private NewsRepository repository;

  public static final NewsRepositoryProvider getInstance() {
    return instance;
  }

  public static final NewsRepository getNewsRepository() {
    return getInstance().repository;
  }

}
