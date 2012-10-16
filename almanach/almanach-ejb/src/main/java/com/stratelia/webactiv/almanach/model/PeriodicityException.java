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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.almanach.model;

import java.util.Date;

import com.stratelia.webactiv.persistence.SilverpeasBean;

public class PeriodicityException extends SilverpeasBean {

  private static final long serialVersionUID = 8352364938259264700L;
  private int periodicityId;
  private Date beginDateException;
  private Date endDateException;

  public PeriodicityException() {
    super();
  }

  public Date getBeginDateException() {
    return beginDateException;
  }

  public void setBeginDateException(Date beginDateException) {
    this.beginDateException = beginDateException;
  }

  public Date getEndDateException() {
    return endDateException;
  }

  public void setEndDateException(Date endDateException) {
    this.endDateException = endDateException;
  }

  public int getPeriodicityId() {
    return periodicityId;
  }

  public void setPeriodicityId(int periodicityId) {
    this.periodicityId = periodicityId;
  }

  @Override
  public String _getTableName() {
    return "SC_Almanach_PeriodicityExcept";
  }
}