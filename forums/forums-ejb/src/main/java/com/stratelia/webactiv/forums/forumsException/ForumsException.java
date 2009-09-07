/*
 * AlmanachException.java
 */

package com.stratelia.webactiv.forums.forumsException;

import com.stratelia.webactiv.util.exception.*;

public class ForumsException extends SilverpeasException {

  public ForumsException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public ForumsException(String callingClass, int errorLevel, String message, String extraParams) {
	super(callingClass, errorLevel, message, extraParams);
  }

  public ForumsException(String callingClass, int errorLevel, String message, Exception nested) {
	super(callingClass, errorLevel, message, nested);
  }

  public ForumsException(String callingClass, int errorLevel, String message, String extraParams,
	                             Exception nested) {
	super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
	 return "forums";
  }
  
}
