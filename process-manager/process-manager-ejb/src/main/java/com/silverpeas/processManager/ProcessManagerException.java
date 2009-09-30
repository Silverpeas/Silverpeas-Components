package com.silverpeas.processManager;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Thrown by the processManager components.
 */
public class ProcessManagerException extends SilverpeasException {
  /**
   * Returns the module name (as known by SilverTrace).
   */
  public String getModule() {
    return "processManager";
  }

  /**
   * Set the caller and the error message
   */
  public ProcessManagerException(String caller, String message) {
    super(caller, SilverpeasException.ERROR, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public ProcessManagerException(String caller, String message,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, nestedException);
  }

  /**
   * Set the caller, infos and the error message
   */
  public ProcessManagerException(String caller, String message, String infos) {
    super(caller, SilverpeasException.ERROR, message, infos);
  }

  /**
   * Set the caller, the error message, infos and the nested exception.
   */
  public ProcessManagerException(String caller, String message, String infos,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, infos, nestedException);
  }

  /**
   * Set the caller, the level and the error message.
   * 
   * Used only by the FatalProcessManagerException
   */
  protected ProcessManagerException(String caller, int level, String message) {
    super(caller, level, message);
  }

  /**
   * Set the caller, the level, the error message and the nested exception.
   * 
   * Used only by the FatalProcessManagerException
   */
  protected ProcessManagerException(String caller, int level, String message,
      Exception nestedException) {
    super(caller, level, message, nestedException);
  }

  /**
   * Set the caller, the level, the error message and infos.
   * 
   * Used only by the FatalProcessManagerException
   */
  protected ProcessManagerException(String caller, int level, String message,
      String infos) {
    super(caller, level, message);
  }

  /**
   * Set the caller, the level, the error message, infos and the nested
   * exception.
   * 
   * Used only by the FatalProcessManagerException
   */
  protected ProcessManagerException(String caller, int level, String message,
      String infos, Exception nestedException) {
    super(caller, level, message, nestedException);
  }
}
