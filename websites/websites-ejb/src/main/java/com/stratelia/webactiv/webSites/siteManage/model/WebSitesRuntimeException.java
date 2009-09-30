package com.stratelia.webactiv.webSites.siteManage.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class WebSitesRuntimeException extends SilverpeasRuntimeException {

  public WebSitesRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public WebSitesRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public WebSitesRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public WebSitesRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "webSites";
  }

}
