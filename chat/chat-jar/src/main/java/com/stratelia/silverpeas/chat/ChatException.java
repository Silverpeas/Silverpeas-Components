package com.stratelia.silverpeas.chat;

/*
 * ChatException.java
 */

import com.stratelia.webactiv.util.exception.*;

public class ChatException extends SilverpeasException {

  public ChatException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public ChatException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public ChatException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public ChatException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "chat";
  }

}
