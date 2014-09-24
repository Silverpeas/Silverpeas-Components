package com.silverpeas.silvercrawler.model;

import org.silverpeas.util.exception.SilverpeasTrappedException;

public class SilverCrawlerForbiddenActionException extends SilverpeasTrappedException {

  /**
   *
   */
  private static final long serialVersionUID = -1605595738572103831L;

  @Override
  public String getModule() {
    return "silverCrawler";
  }

  public SilverCrawlerForbiddenActionException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public SilverCrawlerForbiddenActionException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public SilverCrawlerForbiddenActionException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public SilverCrawlerForbiddenActionException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

}
