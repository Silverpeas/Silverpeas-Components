package com.silverpeas.formsonline.model;

import javax.inject.Inject;

public class FormsOnlineServiceFactory {
  
  private static final FormsOnlineServiceFactory instance = new FormsOnlineServiceFactory();

  @Inject
  private FormsOnlineService service;

  private FormsOnlineServiceFactory() {
  }

  /**
   * Gets a {@link FormsOnlineServiceFactory} instance.
   * @return a factory instance.
   */
  protected static final FormsOnlineServiceFactory getFactory() {
    return instance;
  }

  /**
   * Gets a {@link com.silverpeas.formsonline.model.FormsOnlineService} instance.
   * @return a FormsOnlineService instance.
   */
  public static FormsOnlineService getFormsOnlineService() {
    return getFactory().service;
  }

}