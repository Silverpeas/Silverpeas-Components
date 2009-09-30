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
package com.stratelia.webactiv.almanach.model;

import java.util.Date;

import com.stratelia.webactiv.persistence.SilverpeasBean;

public class Periodicity extends SilverpeasBean {

  public static final int UNITY_DAY = 1;
  public static final int UNITY_WEEK = 2;
  public static final int UNITY_MONTH = 3;
  public static final int UNITY_YEAR = 4;

  private int eventId;
  private int unity; // day, week, month year
  private int frequency;
  private String daysWeekBinary; // longueur 7. ex : 1000001 (lundi et dimanche)
  private int numWeek; // {0=rien, 1, 2, 3, 4, -1=last}
  private int day; // {0=rien, 2=lundi, 3=mardi, etc...}
  private Date untilDatePeriod;

  public Periodicity() {
    super();
    frequency = 1;
    daysWeekBinary = "0000000";
    numWeek = 0;
    day = 0;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public int getUnity() {
    return unity;
  }

  public void setUnity(int unity) {
    this.unity = unity;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public int getNumWeek() {
    return numWeek;
  }

  public void setNumWeek(int numWeek) {
    this.numWeek = numWeek;
  }

  public String getDaysWeekBinary() {
    return daysWeekBinary;
  }

  public void setDaysWeekBinary(String daysWeekBinary) {
    this.daysWeekBinary = daysWeekBinary;
  }

  public Date getUntilDatePeriod() {
    return untilDatePeriod;
  }

  public void setUntilDatePeriod(Date untilDatePeriod) {
    this.untilDatePeriod = untilDatePeriod;
  }

  public String _getTableName() {
    return "SC_Almanach_Periodicity";
  }
}