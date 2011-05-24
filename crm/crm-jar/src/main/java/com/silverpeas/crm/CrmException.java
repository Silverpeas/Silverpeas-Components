package com.silverpeas.crm;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * @author
 */
public class CrmException extends SilverpeasRuntimeException {

  private static final long serialVersionUID = 64009016821924958L;

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @see
   */
  public CrmException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * @see
   */
  public CrmException(String callingClass, int errorLevel, String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param nested
   * @see
   */
  public CrmException(String callingClass, int errorLevel, String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * @param nested
   * @see
   */
  public CrmException(String callingClass, int errorLevel, String message, String extraParams,
      Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getModule() {
    return "crm";
  }

}
