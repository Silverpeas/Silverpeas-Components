package com.silverpeas.wiki.control;

/*
 * ChatException.java
 */

import com.stratelia.webactiv.util.exception.*;

public class WikiException extends SilverpeasException {

  public WikiException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public WikiException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public WikiException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public WikiException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "wiki";
  }

}
