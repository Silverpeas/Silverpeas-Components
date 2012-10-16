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

import java.util.Date;

public class DateOption implements Comparable<DateOption> {

  private String id;
  private Date day;
  private int hour;

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
    int result = super.hashCode();
    result = prime * result + ((day == null) ? 0 : day.hashCode());
    result = prime * result + (int) (hour ^ (hour >>> 32));
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
    return equals((DateOption) obj);
  }

  public boolean equals(DateOption obj) {
    if (day == obj.getDay() &&
        hour == obj.getHour()) {
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
