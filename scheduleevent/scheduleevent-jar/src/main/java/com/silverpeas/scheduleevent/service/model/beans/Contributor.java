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

package com.silverpeas.scheduleevent.service.model.beans;

import java.util.Date;

public class Contributor implements Comparable<Contributor> {

  private String id;
  private ScheduleEvent scheduleEvent;
  private int userId;
  private String userName;
  private Date lastVisit;
  private Date lastValidation;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }

  @Override
  public int compareTo(Contributor o) {
    if (this.getUserId() == o.getUserId()) {
      return 0;
    } else if (this.getUserName() != null && !this.getUserName().equals(o.getUserName())) {
      return this.getUserName().compareTo(o.getUserName());
    } else {
      return 1;
    }
  }

//  @Override
//  public int hashCode() {
//    final int prime = 31;
//    int result = super.hashCode();
//    result = prime * result + ((lastValidation == null) ? 0 : lastValidation.hashCode());
//    result = prime * result + ((lastVisit == null) ? 0 : lastVisit.hashCode());
//    result = prime * result + (int) (userId ^ (userId >>> 32));
//    result = prime * result + ((userName == null) ? 0 : userName.hashCode());
//    return result;
//  }
//
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj)
//      return true;
//    if (obj == null)
//      return false;
//    if (getClass() != obj.getClass())
//      return false;
//    Contributor other = (Contributor) obj;
//    if (lastValidation == null) {
//      if (other.lastValidation != null)
//        return false;
//    } else if (!lastValidation.equals(other.lastValidation))
//      return false;
//    if (lastVisit == null) {
//      if (other.lastVisit != null)
//        return false;
//    } else if (!lastVisit.equals(other.lastVisit))
//      return false;
//    if (userId != other.userId)
//      return false;
//    if (userName == null) {
//      if (other.userName != null)
//        return false;
//    } else if (!userName.equals(other.userName))
//      return false;
//    return true;
//  }

  
  public void setLastVisit(Date lastVisit) {
    this.lastVisit = lastVisit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Contributor other = (Contributor) obj;
    if (id == null) {
      if (other.id != null)
        return false;
      else
        return (userId == other.userId);
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  public Date getLastVisit() {
    return lastVisit;
  }

  public Date getLastValidation() {
    return lastValidation;
  }

  public void setLastValidation(Date lastValidation) {
    this.lastValidation = lastValidation;
  }

  public void setScheduleEvent(ScheduleEvent scheduleEvent) {
    this.scheduleEvent = scheduleEvent;
  }

  public ScheduleEvent getScheduleEvent() {
    return scheduleEvent;
  }

}
