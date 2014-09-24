package com.silverpeas.silvercrawler.model;

import org.silverpeas.util.exception.SilverpeasTrappedException;

public class SilverCrawlerFileUploadException extends SilverpeasTrappedException {


  /**
   *
   */
  private static final long serialVersionUID = 2714744451987446943L;

  @Override
  public String getModule() {
    return "silverCrawler";
  }

  public SilverCrawlerFileUploadException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public SilverCrawlerFileUploadException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public SilverCrawlerFileUploadException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public SilverCrawlerFileUploadException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

}
