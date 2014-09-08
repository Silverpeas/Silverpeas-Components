package org.silverpeas.components.quickinfo.model;

import javax.inject.Inject;

public class QuickInfoServiceFactory {
  
  private static final QuickInfoServiceFactory instance = new QuickInfoServiceFactory();

  @Inject
  private QuickInfoService service;

  private QuickInfoServiceFactory() {
  }

  /**
   * Gets a {@link QuickInfoServiceFactory} instance.
   * @return a factory instance.
   */
  protected static final QuickInfoServiceFactory getFactory() {
    return instance;
  }

  /**
   * Gets a {@link SuggestionBoxService} instance.
   * @return a SuggestionBoxService instance.
   */
  public static QuickInfoService getQuickInfoService() {
    return getFactory().service;
  }

}
