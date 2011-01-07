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
import java.util.List;

/**
 * This Object represents a month value object
 */
public class MonthVO implements Serializable {

  private static final long serialVersionUID = 8536159971527346255L;

  /**
   * List of weeks inside a month
   */
  private List<WeekVO> weeks = null;

  /**
   * Number of month in a year
   */
  private String number = null;

  /**
   * Number of days in this month
   */
  private int nbDays = 0;

  /**
   * @param weeks
   * @param number
   * @param nbDays TODO
   */
  public MonthVO(List<WeekVO> weeks, String number, int nbDays) {
    super();
    this.weeks = weeks;
    this.number = number;
    this.nbDays = nbDays;
  }

  /**
   * @return the weeks
   */
  public List<WeekVO> getWeeks() {
    return weeks;
  }

  /**
   * @param weeks the weeks to set
   */
  public void setWeeks(List<WeekVO> weeks) {
    this.weeks = weeks;
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
   * @return the nbDays
   */
  public int getNbDays() {
    return nbDays;
  }

  /**
   * @param nbDays the nbDays to set
   */
  public void setNbDays(int nbDays) {
    this.nbDays = nbDays;
  }
  
}
