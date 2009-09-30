package com.stratelia.silverpeas.connecteurJDBC.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ConnecteurJDBCRuntimeException extends SilverpeasRuntimeException {

  public ConnecteurJDBCRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public ConnecteurJDBCRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public ConnecteurJDBCRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public ConnecteurJDBCRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "connecteurJDBC";
  }

}
