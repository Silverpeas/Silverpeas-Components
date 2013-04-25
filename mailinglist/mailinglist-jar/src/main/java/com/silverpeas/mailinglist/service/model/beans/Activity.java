/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.mailinglist.service.model.beans;

public class Activity implements Comparable<Activity> {

  private int month;
  private int year;
  private long nbMessages;

  public Activity(long nbMessages, int year, int month) {
    this.month = month;
    this.year = year;
    this.nbMessages = nbMessages;
  }

  public int getMonth() {
    return month;
  }

  public int getYear() {
    return year;
  }

  public long getNbMessages() {
    return nbMessages;
  }

  @Override
  public int compareTo(Activity other) {
    if (other == null) {
      return -1;
    }
    if (this.equals(other)) {
      return 0;
    }
    if (year == other.getYear()) {
      return month - other.getMonth();
    }
    return year - other.getYear();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o.getClass() != this.getClass()) {
      return false;
    }
    Activity other = (Activity) o;
    return (year == other.getYear() && month == other.getMonth() && nbMessages == other
        .getNbMessages());
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 79 * hash + this.month;
    hash = 79 * hash + this.year;
    hash = 79 * hash + (int) (this.nbMessages ^ (this.nbMessages >>> 32));
    return hash;
  }
}
