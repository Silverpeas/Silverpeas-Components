/*
 * AlmanachException.java
 */

package com.stratelia.webactiv.almanach.control.ejb;

import com.stratelia.webactiv.util.exception.*;

public class AlmanachException extends SilverpeasException {

  public AlmanachException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public AlmanachException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public AlmanachException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public AlmanachException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "almanach";
  }

}
