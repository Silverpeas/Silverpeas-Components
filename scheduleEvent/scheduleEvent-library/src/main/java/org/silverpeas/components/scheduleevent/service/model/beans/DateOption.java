/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.scheduleevent.service.model.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "sc_scheduleevent_options")
public class DateOption implements Comparable<DateOption>, Serializable {

  public static final int MORNING_BEGIN_HOUR = 8;
  public static final int MORNING_END_HOUR = 12;
  public static final int AFTERNOON_BEGIN_HOUR = 14;
  public static final int AFTERNOON_END_HOUR = 18;

  @Id
  private String id;
  @Column(name = "optionday")
  @Temporal(TemporalType.TIMESTAMP)
  private Date day;
  @Column(name = "optionhour")
  private int hour;

  @PrePersist
  protected void setUpId() {
    id = UUID.randomUUID().toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getDay() {
    return day;
  }

  public void setDay(Date day) {
    this.day = day;
  }

  public int getHour() {
    return hour;
  }

  public void setHour(int hour) {
    this.hour = hour;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = prime + ((day == null) ? 0 : day.hashCode());
    result = prime * result + (int) (hour ^ (hour >>> 31));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DateOption option = (DateOption)obj;
    if (day.equals(option.getDay()) && hour == option.getHour()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int compareTo(DateOption obj) {
    if (this.equals(obj)) {
      return 0;
    } else if (day.equals(obj.getDay())) {
      if (hour > obj.getHour()) {
        return 1;
      } else {
        return -1;
      }
    } else {
      return day.compareTo(obj.getDay());
    }
  }
}
