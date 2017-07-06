/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.scheduleevent.service.model.beans;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "sc_scheduleevent_response")
public class Response implements Serializable {

  @Id
  private String id;
  @ManyToOne
  @JoinColumn(name = "scheduleeventid", nullable = false)
  private ScheduleEvent scheduleEvent;
  private int userId;
  private String optionId;

  @PrePersist
  protected void setUpId() {
    id = UUID.randomUUID().toString();
  }

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

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + this.userId;
    hash = 37 * hash + (this.optionId != null ? this.optionId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Response response = (Response)obj;
    if (userId == response.getUserId() && optionId.equals(response.getOptionId())) {
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
