package com.silverpeas.rssAgregator.model;

import com.stratelia.webactiv.util.exception.*;

public class RssAgregatorException extends SilverpeasException {
  public RssAgregatorException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public RssAgregatorException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public RssAgregatorException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public RssAgregatorException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "rssAgregator";
  }
}