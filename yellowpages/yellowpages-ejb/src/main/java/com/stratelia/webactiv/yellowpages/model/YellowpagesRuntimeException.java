/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.yellowpages.model;

import com.stratelia.webactiv.util.exception.*;

/*
 * CVS Informations
 *
 * $Id: YellowpagesRuntimeException.java,v 1.1.1.1 2002/08/06 14:48:02 nchaix Exp $
 *
 * $Log: YellowpagesRuntimeException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:48:02  nchaix
 * no message
 *
 * Revision 1.7  2002/01/22 11:12:20  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class YellowpagesRuntimeException extends SilverpeasRuntimeException {

  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public YellowpagesRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * 
   * @see
   */
  public YellowpagesRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param nested
   * 
   * @see
   */
  public YellowpagesRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * @param nested
   * 
   * @see
   */
  public YellowpagesRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getModule() {
    return "yellowpages";
  }

}
