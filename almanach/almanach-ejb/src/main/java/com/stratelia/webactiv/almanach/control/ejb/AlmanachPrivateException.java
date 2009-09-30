/*
 * AlmanachException.java
 *
 * Created on 23 août 2001, 11:51
 */

package com.stratelia.webactiv.almanach.control.ejb;

/**
 * 
 * @author groccia
 * @version
 */
public class AlmanachPrivateException extends Exception {

  /** Creates new AlmanachException */
  public AlmanachPrivateException(String message) {
    super(message);
  }

  public String getModule() {
    return "almanach";
  }
}