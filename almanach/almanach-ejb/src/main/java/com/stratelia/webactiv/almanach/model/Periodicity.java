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