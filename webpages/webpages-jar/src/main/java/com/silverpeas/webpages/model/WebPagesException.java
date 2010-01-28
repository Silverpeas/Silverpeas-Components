/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

  private static final long serialVersionUID = 325345510438177278L;

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
