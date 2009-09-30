package com.silverpeas.blog.model;

import com.stratelia.webactiv.util.exception.*;

public class BlogRuntimeException extends SilverpeasRuntimeException {
  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public BlogRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public BlogRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public BlogRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public BlogRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "Blog";
  }

}
