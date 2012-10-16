/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.projectManager.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * This Object represents a day value object
 */
public class DayVO implements Serializable {

  private static final long serialVersionUID = 4711346434253108166L;

  /**
   * Number of day in a month with two digits character
   */
  private String number = null;

  /**
   * First day character
   */
  private String firstDayChar = null;
  
  /**
   * Date
   */
  private Date day = null;

  /**
   * @param number
   * @param firstDayChar
   * @param day TODO
   */
  public DayVO(String number, String firstDayChar, Date day) {
    super();
    this.number = number;
    this.firstDayChar = firstDayChar;
    this.day = day;
  }

  /**
   * @return the number
   */
  public String getNumber() {
    return number;
  }

  /**
   * @param number the number to set
   */
  public void setNumber(String number) {
    this.number = number;
  }

  /**
   * @return the firstDayChar
   */
  public String getFirstDayChar() {
    return firstDayChar;
  }

  /**
   * @param firstDayChar the firstDayChar to set
   */
  public void setFirstDayChar(String firstDayChar) {
    this.firstDayChar = firstDayChar;
  }

  /**
   * @return the day
   */
  public Date getDay() {
    return day;
  }

  /**
   * @param day the day to set
   */
  public void setDay(Date day) {
    this.day = day;
  }
}
