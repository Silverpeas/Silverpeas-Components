package com.silverpeas.rssAgregator;

import com.stratelia.webactiv.util.exception.*;

public class RssAgregatorRuntimeException extends SilverpeasRuntimeException {
  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public RssAgregatorRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public RssAgregatorRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public RssAgregatorRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public RssAgregatorRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "RssAgregator";
  }

}
