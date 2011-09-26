package com.silverpeas.silvercrawler.model;

import com.stratelia.webactiv.util.exception.SilverpeasTrappedException;

public class SilverCrawlerFolderRenameException extends SilverpeasTrappedException {


  /**
   *
   */
  private static final long serialVersionUID = 2714744451987446943L;

  @Override
  public String getModule() {
    return "silverCrawler";
  }

  public SilverCrawlerFolderRenameException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public SilverCrawlerFolderRenameException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public SilverCrawlerFolderRenameException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public SilverCrawlerFolderRenameException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

}
