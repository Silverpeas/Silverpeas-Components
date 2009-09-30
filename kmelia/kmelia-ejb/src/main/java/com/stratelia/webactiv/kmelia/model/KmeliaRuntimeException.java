/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.kmelia.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/*
 * CVS Informations
 *
 * $Id: KmeliaRuntimeException.java,v 1.4 2007/06/14 11:23:05 neysseri Exp $
 *
 * $Log: KmeliaRuntimeException.java,v $
 * Revision 1.4  2007/06/14 11:23:05  neysseri
 * no message
 *
 * Revision 1.3  2007/04/23 16:45:06  neysseri
 * no message
 *
 * Revision 1.2  2007/04/20 14:30:08  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:57  nchaix
 * no message
 *
 * Revision 1.3  2002/01/22 11:07:02  mguillem
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
public class KmeliaRuntimeException extends SilverpeasRuntimeException {

  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public KmeliaRuntimeException(String callingClass, int errorLevel,
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
  public KmeliaRuntimeException(String callingClass, int errorLevel,
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
  public KmeliaRuntimeException(String callingClass, int errorLevel,
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
  public KmeliaRuntimeException(String callingClass, int errorLevel,
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
    return "kmelia";
  }

}
