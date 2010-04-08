package com.silverpeas.dataWarning;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class DataWarningRuntimeException extends SilverpeasRuntimeException {
  
  public DataWarningRuntimeException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public DataWarningRuntimeException(String callingClass, int errorLevel, String message, String extraParams) {
	super(callingClass, errorLevel, message, extraParams);
  }

  public DataWarningRuntimeException(String callingClass, int errorLevel, String message, Exception nested) {
	super(callingClass, errorLevel, message, nested);
  }

  public DataWarningRuntimeException(String callingClass, int errorLevel, String message, String extraParams,
	                             Exception nested) {
	super(callingClass, errorLevel, message, extraParams, nested);
  }
	
  public String getModule() {
    return "dataWarning";
  }


}

