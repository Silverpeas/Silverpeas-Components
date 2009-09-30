package com.silverpeas.mydb.exception;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * MDB exception.
 * 
 * @author Antoine HEDIN
 */
public class MyDBException extends SilverpeasException {

  public MyDBException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public MyDBException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "myDB";
  }

}
