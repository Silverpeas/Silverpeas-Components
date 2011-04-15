/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.almanach.control;

import java.util.Calendar;
import java.util.Date;

/**
 * An almanach day is a day within a calendar.
 */
public class AlmanachDay {

  private int year;
  private int month;
  private int dayOfMonth;

  /**
   * Constructs a new AlmanachDay instance from the specified date.
   * @param date the date of the day.
   */
  public AlmanachDay(final Date aDay) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(aDay);
    year = calendar.get(Calendar.YEAR);
    month = calendar.get(Calendar.MONTH);
    dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Gets the day of the month (from 1 to 31).
   * @return the number of the day in the month.
   */
  public int getDayOfMonth() {
    return dayOfMonth;
  }

  /**
   * Gets the month.
   * @return the month: 1 for january, 2 for february, and so one.
   */
  public int getMonth() {
    return month;
  }

  /**
   * Gets the year.
   * @return the year in 4 digits.
   */
  public int getYear() {
    return year;
  }

  /**
   * Gets this day as a Date instance.
   * @return the Date representation of this day.
   */
  public Date getDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }
}
