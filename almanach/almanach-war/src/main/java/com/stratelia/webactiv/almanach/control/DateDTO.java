/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Date;

/**
 * A DTO on a date in the alamanch. The date is made up of the day in year and optionally of the
 * time in the day. It is a way to carry the information pertinence about the date to render in an
 * almanach.
 */
public class DateDTO {

  private String day;
  private String time;

  /**
   * Constructs a new DateDTO from the specified day in year and time in day.
   * @param dayInYear the day in year in the ISO 8601 pattern: yyyy-MM-dd.
   * @param timeInDay  the time in day in the following ISO 8601 pattern HH:mm.
   */
  public DateDTO(final String dayInYear, final String timeInDay) {
    this.day = dayInYear;
    this.time = timeInDay;
  }

  /**
   * Gets the day in year of this date.
   * @return the day in year.
   */
  public String getDayInYear() {
    return day;
  }

  /**
   * Gets the time in day of this date.
   * @return the time in day.
   */
  public String getTimeInDay() {
    return time;
  }

  /**
   * Gets the date in one of the ISO 8601 pattern: yyyy-MM-ddTHH:mm or yyyy-MM-dd if the time in
   * day isn't set.
   * @return the date in one of the ISO 8601 pattern.
   */
  public String getISO8601Date() {
    String date = day;
    if (StringUtil.isDefined(time)) {
      date += "T" + time;
    }
    return date;
  }

  /**
   * Gets the date as a Date instance.
   * @return the date carrying by this DTO.
   */
  public Date getDate() {
    try {
      return DateUtil.parseISO8601Date(getISO8601Date());
    } catch (Exception e) {
      SilverTrace.warn("almanach", getClass().getSimpleName() + "getDate()", "root.EX_NO_MESSAGES",
          e);
      return null;
    }
  }
}
