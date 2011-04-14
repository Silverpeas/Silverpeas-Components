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

package com.silverpeas.scheduleevent.service.model.beans;

public class Response {

  private String id;
  private ScheduleEvent scheduleEvent;
  private int userId;
  private String optionId;

  public String getOptionId() {
    return optionId;
  }

  public void setOptionId(String optionId) {
    this.optionId = optionId;
  }

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

  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (int) (userId ^ (userId >>> 32));
    result = prime * result + ((optionId == null) ? 0 : optionId.hashCode());
    return result;
  }

  public boolean equals(Response obj) {
    if (userId == obj.getUserId() &&
        optionId.equals(obj.getOptionId())) {
      return true;
    } else {
      return false;
    }
  }

  public void setScheduleEvent(ScheduleEvent scheduleEvent) {
    this.scheduleEvent = scheduleEvent;
  }

  public ScheduleEvent getScheduleEvent() {
    return scheduleEvent;
  }

}
