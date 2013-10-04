/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.blog.model;

import java.io.Serializable;

public class Archive implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;
  private String monthId; // between 0 and 11
  private String year;
  private String beginDate;
  private String endDate;

  public Archive() {
  }

  public Archive(String name, String beginningDate, String endDate) {
    super();
    this.name = name;
    this.beginDate = beginningDate;
    this.endDate = endDate;
  }

  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String beginDate) {
    this.beginDate = beginDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getMonthId() {
    return monthId;
  }

  public void setMonthId(String monthId) {
    this.monthId = monthId;
  }

  @Override
  public boolean equals(Object arg0) {
    if (arg0 == null || !(arg0 instanceof Archive))
      return false;
    Archive archive = (Archive) arg0;
    return archive.getYear().equals(this.getYear()) &&
        archive.getMonthId().equals(this.getMonthId());
  }
}
