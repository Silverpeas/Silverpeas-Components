package com.silverpeas.resourcesmanager.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ResourcesManagerRuntimeException extends
    SilverpeasRuntimeException {
  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public ResourcesManagerRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public ResourcesManagerRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public ResourcesManagerRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public ResourcesManagerRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "ResourcesManager";
  }

}