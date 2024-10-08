/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.datawarning.model;

import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;

@SuppressWarnings("deprecation")
public class DataWarningScheduler extends SilverpeasBean {

  private static final long serialVersionUID = -406809133902097195L;
  public static final int SCHEDULER_N_TIMES_MOMENT_HOUR = 0;
  public static final int SCHEDULER_N_TIMES_MOMENT_DAY = 1;
  public static final int SCHEDULER_N_TIMES_MOMENT_WEEK = 2;
  public static final int SCHEDULER_N_TIMES_MOMENT_MONTH = 3;
  public static final int SCHEDULER_N_TIMES_MOMENT_YEAR = 4;
  public static final int SCHEDULER_STATE_OFF = 0;
  public static final int SCHEDULER_STATE_ON = 1;
  private static final int MINUTES_IN_HOUR = 60;
  private static final int HOURS_IN_DAY = 24;
  private static final int DAYS_IN_WEEK = 7;
  private static final int MONTHS_IN_YEAR = 12;
  private static final int MIN_DAYS_IN_MONTH = 28;
  private String instanceId;
  private int numberOfTimes;
  private int numberOfTimesMoment;
  private int minits;
  private int hours;
  private int dayOfWeek;
  private int dayOfMonth;
  private int theMonth;
  private int schedulerState;
  private long wakeUp = 0;

  public DataWarningScheduler() {
    super();
    schedulerState = SCHEDULER_STATE_OFF;
  }

  public DataWarningScheduler copy() {
    DataWarningScheduler newOne;
    try {
      newOne = (DataWarningScheduler) super.clone();
    } catch (CloneNotSupportedException e) {
      SilverLogger.getLogger(this).silent(e);
      newOne = new DataWarningScheduler();
    }

    newOne.instanceId = instanceId;
    newOne.numberOfTimes = numberOfTimes;
    newOne.numberOfTimesMoment = numberOfTimesMoment;
    newOne.minits = minits;
    newOne.hours = hours;
    newOne.dayOfWeek = dayOfWeek;
    newOne.dayOfMonth = dayOfMonth;
    newOne.theMonth = theMonth;
    newOne.schedulerState = schedulerState;
    newOne.wakeUp = wakeUp;
    newOne.setPK(getPK());

    return newOne;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public long getWakeUp() {
    return wakeUp;
  }

  public void setWakeUp(long wakeUp) {
    this.wakeUp = wakeUp;
  }

  public int getNumberOfTimes() {
    return numberOfTimes;
  }

  public void setNumberOfTimes(int numberOfTimes) {
    this.numberOfTimes = numberOfTimes;
  }

  public int getNumberOfTimesMoment() {
    return numberOfTimesMoment;
  }

  public void setNumberOfTimesMoment(int numberOfTimesMoment) {
    this.numberOfTimesMoment = numberOfTimesMoment;
  }

  public int getMinits() {
    return minits;
  }

  public void setMinits(int minits) {
    this.minits = minits;
  }

  public int getHours() {
    return hours;
  }

  public void setHours(int hours) {
    this.hours = hours;
  }

  public int getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(int dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }

  public int getDayOfMonth() {
    return dayOfMonth;
  }

  public void setDayOfMonth(int dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
  }

  public int getTheMonth() {
    return theMonth;
  }

  public void setTheMonth(int month) {
    this.theMonth = month;
  }

  public int getSchedulerState() {
    return schedulerState;
  }

  public void setSchedulerState(int schedulerState) {
    this.schedulerState = schedulerState;
  }

  @Override
  @NonNull
  protected String getTableName() {
    return "SC_DataWarning_Scheduler";
  }

// Cron String creation
  public String createCronString() {
    String cron;
    if (numberOfTimes == 1) {
      cron = cronForOneTime();
    } else {
      cron = cronForSeveralTimes();
    }
    return cron;
  }

  private String cronForSeveralTimes() {
    String cron = null;
    switch (numberOfTimesMoment) {
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR:
        cron = cronSystem(MINUTES_IN_HOUR, numberOfTimes, false) + " * * * ?";
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY:
        cron = "* " + cronSystem(HOURS_IN_DAY, numberOfTimes, false) + " * * ?";
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK:
        cron = "* 12 ? * " + cronSystem(DAYS_IN_WEEK, numberOfTimes, false);
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH:
        cron = "* 12 " + cronSystem(MIN_DAYS_IN_MONTH, numberOfTimes, true) + " * ?";
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR:
        cron = "* 12 1 " + cronSystem(MONTHS_IN_YEAR, numberOfTimes, true) + " ?";
        break;
      default:
        break;
    }
    return cron;
  }

  private String cronForOneTime() {
    String cron = null;
    switch (numberOfTimesMoment) {
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR:
        cron = minits + " * * * ?";
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY:
        cron = minits + " " + hours + " * * ?";
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK:
        cron = minits + " " + hours + " ? * " + dayOfWeek;
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH:
        cron = minits + " " + hours + " " + (dayOfMonth + 1) + " * ?";
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR:
        cron = minits + " " + hours + " " + (dayOfMonth + 1) + " " + (theMonth + 1) + " ?";
        break;
      default:
        break;
    }
    return cron;
  }

  private String cronSystem(int nb, int numberOfTimes, boolean startFromOne) {
    int multiplicateur = nb / numberOfTimes;
    StringBuilder temp = new StringBuilder();
    for (int i = 1; i < numberOfTimes + 1; i++) {
      if (startFromOne) {
        temp.append(multiplicateur * i).append(",");
      } else {
        temp.append((multiplicateur * i) - 1).append(",");
      }
    }
    temp.deleteCharAt(temp.length() - 1);
    return temp.toString();
  }

}