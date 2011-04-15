/**
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
import java.util.Calendar;
import java.util.Collection;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;

public class Periodicity extends SilverpeasBean {

  private static final long serialVersionUID = -5666462083577316755L;
  public static final int UNIT_NONE = 0;
  public static final int UNIT_DAY = 1;
  public static final int UNIT_WEEK = 2;
  public static final int UNIT_MONTH = 3;
  public static final int UNIT_YEAR = 4;
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
    if (untilDatePeriod == null) {
      return null;
    }
    return new Date(untilDatePeriod.getTime());
  }

  public void setUntilDatePeriod(Date untilDatePeriod) {
    if (untilDatePeriod == null) {
      this.untilDatePeriod = null;
    } else {
      Calendar until = Calendar.getInstance();
      until.setTime(untilDatePeriod);
      until.set(Calendar.HOUR_OF_DAY, 23);
      until.set(Calendar.MINUTE, 59);
      until.set(Calendar.SECOND, 59);
      until.set(Calendar.MILLISECOND, 999);      
      this.untilDatePeriod = until.getTime();
    }
  }

  @Override
  public String _getTableName() {
    return "SC_Almanach_Periodicity";
  }
  
  
  public RRule generateRecurrenceRule() {
    String typeRecurence = Recur.DAILY;
    if (this.getUnity() == UNIT_WEEK) {
      typeRecurence = Recur.WEEKLY;
    } else if (this.getUnity() == UNIT_MONTH) {
      typeRecurence = Recur.MONTHLY;
    } else if (this.getUnity() == UNIT_YEAR) {
      typeRecurence = Recur.YEARLY;
    }
    DateTime untilDate = null;
    if (this.getUntilDatePeriod() != null) {
      untilDate = new DateTime(this.getUntilDatePeriod());
    }
    Recur recur = new Recur(typeRecurence, untilDate);
    recur.setInterval(this.getFrequency());

    if (Recur.WEEKLY.equals(typeRecurence)) {

      if (this.getDaysWeekBinary().charAt(0) == '1') {// Monday
        recur.getDayList().add(WeekDay.MO);
      }
      if (this.getDaysWeekBinary().charAt(1) == '1') {// Tuesday
        recur.getDayList().add(WeekDay.TU);
      }
      if (this.getDaysWeekBinary().charAt(2) == '1') {
        recur.getDayList().add(WeekDay.WE);
      }
      if (this.getDaysWeekBinary().charAt(3) == '1') {
        recur.getDayList().add(WeekDay.TH);
      }
      if (this.getDaysWeekBinary().charAt(4) == '1') {
        recur.getDayList().add(WeekDay.FR);
      }
      if (this.getDaysWeekBinary().charAt(5) == '1') {
        recur.getDayList().add(WeekDay.SA);
      }
      if (this.getDaysWeekBinary().charAt(6) == '1') {
        recur.getDayList().add(WeekDay.SU);
      }

    } else if (Recur.MONTHLY.equals(typeRecurence)) {
      if (this.getNumWeek() != 0) {// option choix du jour de la semaine
        if (this.getDay() == java.util.Calendar.MONDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.MO, this.getNumWeek()));
        } else if (this.getDay() == java.util.Calendar.TUESDAY) {// Tuesday
          recur.getDayList().add(
              new WeekDay(WeekDay.TU, this.getNumWeek()));
        } else if (this.getDay() == java.util.Calendar.WEDNESDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.WE, this.getNumWeek()));
        } else if (this.getDay() == java.util.Calendar.THURSDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.TH, this.getNumWeek()));
        } else if (this.getDay() == java.util.Calendar.FRIDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.FR, this.getNumWeek()));
        } else if (this.getDay() == java.util.Calendar.SATURDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.SA, this.getNumWeek()));
        } else if (this.getDay() == java.util.Calendar.SUNDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.SU, this.getNumWeek()));
        }
      }
    }
    RRule rrule = new RRule(recur);
    return rrule;
  }
  
  
  public ExDate generateExceptionDate(Collection<PeriodicityException> listException) {
    // Exceptions de périodicité
    DateList dateList = new DateList();
    java.util.Calendar dateException = java.util.Calendar.getInstance();
    java.util.Calendar dateFinalException = java.util.Calendar.getInstance();
    for(PeriodicityException periodicityException : listException){
      dateException.setTime(periodicityException.getBeginDateException());
      dateFinalException.setTime(periodicityException.getEndDateException());
      while (dateException.before(dateFinalException) || dateException.equals(dateFinalException)) {
        dateList.add(new DateTime(dateException.getTime()));
        dateException.add(java.util.Calendar.DATE, 1);
      }
    }
    ExDate exDate = new ExDate(dateList);
    return exDate;
  }
}
