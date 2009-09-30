/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.newsEdito;

/**
 * To manage the ejb CreateException
 * 
 * @author Sébastien Antonio
 */
public class CreateNewsEditoException extends NewsEditoException {

  // constructors

  /**
   * Constructor which calls the super constructor
   * 
   * @param callingClass
   *          (String) the name of the module which catchs the Exception
   * @param errorLevel
   *          (int) the level error of the exception
   * @param message
   *          (String) the level of the exception label
   * @param extraParams
   *          (String) the generic exception message
   * @param nested
   *          (Exception) the exception catched
   */
  public CreateNewsEditoException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
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
  public CreateNewsEditoException(String callingClass, int errorLevel,
      String message, String extraParams) {
    this(callingClass, errorLevel, message, extraParams, null);
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
  public CreateNewsEditoException(String callingClass, int errorLevel,
      String message, Exception nested) {
    this(callingClass, errorLevel, message, "", nested);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * 
   * @see
   */
  public CreateNewsEditoException(String callingClass, int errorLevel,
      String message) {
    this(callingClass, errorLevel, message, "", null);
  }

}
