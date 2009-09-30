package com.silverpeas.processManager;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Thrown when a fatal error occured in a processManager component.
 */
public class ProcessManagerFatalException extends ProcessManagerException {
  /**
   * Set the caller and the error message
   */
  public ProcessManagerFatalException(String caller, String message) {
    super(caller, SilverpeasException.ERROR, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public ProcessManagerFatalException(String caller, String message,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, nestedException);
  }

  /**
   * Set the caller, infos and the error message
   */
  public ProcessManagerFatalException(String caller, String message,
      String infos) {
    super(caller, SilverpeasException.ERROR, message, infos);
  }

  /**
   * Set the caller, the error message, infos and the nested exception.
   */
  public ProcessManagerFatalException(String caller, String message,
      String infos, Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, infos, nestedException);
  }
}
