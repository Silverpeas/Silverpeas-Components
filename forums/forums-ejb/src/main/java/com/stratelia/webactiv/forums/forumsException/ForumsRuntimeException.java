/*
 * ForumsRuntimeException.java
 *
 * Created on 10 Decembre 2001
 */

package com.stratelia.webactiv.forums.forumsException;

import com.stratelia.webactiv.util.exception.*;

public class ForumsRuntimeException extends SilverpeasRuntimeException {

  public ForumsRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public ForumsRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public ForumsRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public ForumsRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "forums";
  }

}
