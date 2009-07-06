/*
 * AlmanachRuntimeException.java
 *
 * Created on 23 août 2001, 11:51
 */
 
package com.stratelia.webactiv.almanach.control.ejb;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/** 
 *
 * @author  groccia
 * @version 
 */
public class AlmanachRuntimeException extends SilverpeasRuntimeException
{


  public AlmanachRuntimeException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public AlmanachRuntimeException(String callingClass, int errorLevel, String message, String extraParams) {
	super(callingClass, errorLevel, message, extraParams);
  }

  public AlmanachRuntimeException(String callingClass, int errorLevel, String message, Exception nested) {
	super(callingClass, errorLevel, message, nested);
  }

  public AlmanachRuntimeException(String callingClass, int errorLevel, String message, String extraParams,
	                             Exception nested) {
	super(callingClass, errorLevel, message, extraParams, nested);
  }
  
  public String getModule() {
    return "almanach";
  }
}