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
package com.silverpeas.dataWarning.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class DataWarningScheduler extends SilverpeasBean {

  private static final long serialVersionUID = -406809133902097195L;
  public static final int SCHEDULER_N_TIMES_MOMENT_HOUR = 0;
  public static final int SCHEDULER_N_TIMES_MOMENT_DAY = 1;
  public static final int SCHEDULER_N_TIMES_MOMENT_WEEK = 2;
  public static final int SCHEDULER_N_TIMES_MOMENT_MONTH = 3;
  public static final int SCHEDULER_N_TIMES_MOMENT_YEAR = 4;
  public static final int SCHEDULER_STATE_OFF = 0;
  public static final int SCHEDULER_STATE_ON = 1;
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

  public DataWarningScheduler(String instanceId, int numberOfTimes, int numberOfTimesMoment,
      int minits, int hours, int dayOfWeek, int dayOfMonth, int month, int schedulerState) {
    super();
    this.instanceId = instanceId;
    this.numberOfTimes = numberOfTimes;
    this.numberOfTimesMoment = numberOfTimesMoment;
    this.minits = minits;
    this.hours = hours;
    this.dayOfWeek = dayOfWeek;
    this.dayOfMonth = dayOfMonth;
    this.theMonth = month;
    this.schedulerState = schedulerState;
    this.wakeUp = 0;
  }

  @Override
  public Object clone() {
    DataWarningScheduler newOne = new DataWarningScheduler();

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
  public String _getTableName() {
    return "SC_DataWarning_Scheduler";
  }

// Cron String creation
  public String createCronString() {
    String retour = null;
    if (numberOfTimes == 1) {
      switch (numberOfTimesMoment) {
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR:
          retour = minits + " * * * ?";
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY:
          retour = minits + " " + hours + " * * ?";
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK:
          retour = minits + " " + hours + " ? * " + dayOfWeek;
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH:
          retour = minits + " " + hours + " " + (dayOfMonth + 1) + " * ?";
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR:
          retour = minits + " " + hours + " " + (dayOfMonth + 1) + " " + (theMonth + 1) + " ?";
          break;
      }
    } else {
      switch (numberOfTimesMoment) {
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR:
          retour = cronSystem(60, numberOfTimes, false) + " * * * ?";
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY:
          retour = "* " + cronSystem(24, numberOfTimes, false) + " * * ?";
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK:
          retour = "* 12 ? * " + cronSystem(7, numberOfTimes, false);
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH:
          retour = "* 12 " + cronSystem(28, numberOfTimes, true) + " * ?";
          break;
        case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR:
          retour = "* 12 1 " + cronSystem(12, numberOfTimes, true) + " ?";
          break;
      }
    }
    return retour;
  }

  private String cronSystem(int nb, int numberOfTimes, boolean startFromOne) {
    int multiplicateur = nb / numberOfTimes;
    String temp = "";
    for (int i = 1; i < numberOfTimes + 1; i++) {
      if (startFromOne) {
        temp += (multiplicateur * i) + ",";
      } else {
        temp += (multiplicateur * i) - 1 + ",";
      }
    }
    temp = temp.substring(0, temp.length() - 1);
    return temp;
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }
}