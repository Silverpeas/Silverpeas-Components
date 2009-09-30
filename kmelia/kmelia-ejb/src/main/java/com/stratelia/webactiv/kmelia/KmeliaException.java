package com.stratelia.webactiv.kmelia;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class KmeliaException extends SilverpeasException {
  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public KmeliaException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public KmeliaException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public KmeliaException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public KmeliaException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "kmelia";
  }

}
