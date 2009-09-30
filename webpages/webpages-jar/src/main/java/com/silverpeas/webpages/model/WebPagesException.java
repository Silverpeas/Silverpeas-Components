/*
 * Created on 14 avr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.silverpeas.webpages.model;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author sdevolder
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class WebPagesException extends SilverpeasException {

  /**
   * @param callingClass
   * @param errorLevel
   * @param message
   */
  public WebPagesException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param nested
   */
  public WebPagesException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   */
  public WebPagesException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * @param nested
   */
  public WebPagesException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
    // TODO Auto-generated constructor stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratelia.webactiv.util.exception.FromModule#getModule()
   */
  public String getModule() {
    // TODO Auto-generated method stub
    return null;
  }

}
