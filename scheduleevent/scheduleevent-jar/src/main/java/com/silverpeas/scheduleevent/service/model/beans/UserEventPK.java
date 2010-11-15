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

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserEventPK implements Serializable {

  private String userId;
  private String scheduleEventId;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getScheduleEventId() {
    return scheduleEventId;
  }

  public void setScheduleEventId(String scheduleEventId) {
    this.scheduleEventId = scheduleEventId;
  }

  @Override
  public boolean equals(Object obj) {
    UserEventPK pk = (UserEventPK) obj;
    if (userId.equals(pk.getUserId()) &&
        scheduleEventId.equals(pk.getScheduleEventId())) {
      return true;
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    result = prime * result + ((scheduleEventId == null) ? 0 : scheduleEventId.hashCode());
    return result;
  }

}
