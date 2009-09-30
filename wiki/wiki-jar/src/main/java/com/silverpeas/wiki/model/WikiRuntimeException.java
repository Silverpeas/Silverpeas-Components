package com.silverpeas.wiki.model;

import com.stratelia.webactiv.util.exception.*;

public class WikiRuntimeException extends SilverpeasRuntimeException {
  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public WikiRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public WikiRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public WikiRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public WikiRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "Wiki";
  }

}