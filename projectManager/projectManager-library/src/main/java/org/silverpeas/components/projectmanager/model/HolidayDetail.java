/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.projectmanager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author neysseri
 */
public class HolidayDetail implements Serializable {
  private static final long serialVersionUID = 5791543598843818180L;
  private Date holidayDate = null;
  private String instanceId = null;
  private int fatherId = -1;

  public HolidayDetail(Date holidayDate, int fatherId, String instanceId) {
    setDate(holidayDate);
    setInstanceId(instanceId);
    setFatherId(fatherId);
  }

  /**
   * @return
   */
  public int getFatherId() {
    return fatherId;
  }

  /**
   * @return
   */
  public Date getDate() {
    return holidayDate;
  }

  /**
   * @return
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param i
   */
  public void setFatherId(int i) {
    fatherId = i;
  }

  /**
   * @param date
   */
  public void setDate(Date date) {
    holidayDate = date;
  }

  /**
   * @param string
   */
  public void setInstanceId(String string) {
    instanceId = string;
  }

}
