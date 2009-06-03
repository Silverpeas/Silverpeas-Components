/*
 * WebSitesException.java
 */

package com.stratelia.webactiv.webSites.control;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class WebSitesException extends SilverpeasException {

  public WebSitesException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public WebSitesException(String callingClass, int errorLevel, String message, String extraParams) {
	super(callingClass, errorLevel, message, extraParams);
  }

  public WebSitesException(String callingClass, int errorLevel, String message, Exception nested) {
	super(callingClass, errorLevel, message, nested);
  }

  public WebSitesException(String callingClass, int errorLevel, String message, String extraParams,
	                             Exception nested) {
	super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
	 return "webSites";
  }
  
}
