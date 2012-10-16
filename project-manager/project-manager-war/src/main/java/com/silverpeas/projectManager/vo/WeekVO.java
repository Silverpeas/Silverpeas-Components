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
import java.util.List;

/**
 * This Object represents a Week value object
 */
public class WeekVO implements Serializable {

  private static final long serialVersionUID = 8536159971527346255L;

  /**
   * List of days inside a week
   */
  private List<DayVO> days = null;

  /**
   * Number of week in a year
   */
  private String number = null;

  /**
   * @param days
   * @param number
   */
  public WeekVO(List<DayVO> days, String number) {
    super();
    this.days = days;
    this.number = number;
  }

  /**
   * @return the days
   */
  public List<DayVO> getDays() {
    return days;
  }

  /**
   * @param days the days to set
   */
  public void setDays(List<DayVO> days) {
    this.days = days;
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
}
