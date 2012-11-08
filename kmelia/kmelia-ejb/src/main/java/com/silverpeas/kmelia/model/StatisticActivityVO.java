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

package com.silverpeas.kmelia.model;

import java.io.Serializable;

public class StatisticActivityVO implements Serializable {

  /**
   * Serializable purpose
   */
  private static final long serialVersionUID = -2115611579875072384L;

  private Integer createdPublicationNumber = null;
  private Integer modifiedPublicationNumber = null;

  /**
   * @param createdPublicationNumber
   * @param modifiedPublicationNumber
   */
  public StatisticActivityVO(Integer createdPublicationNumber, Integer modifiedPublicationNumber) {
    super();
    this.createdPublicationNumber = createdPublicationNumber;
    this.modifiedPublicationNumber = modifiedPublicationNumber;
  }

  /**
   * @return the createdPublicationNumber
   */
  public Integer getCreatedPublicationNumber() {
    return createdPublicationNumber;
  }

  /**
   * @param createdPublicationNumber the createdPublicationNumber to set
   */
  public void setCreatedPublicationNumber(Integer createdPublicationNumber) {
    this.createdPublicationNumber = createdPublicationNumber;
  }

  /**
   * @return the modifiedPublicationNumber
   */
  public Integer getModifiedPublicationNumber() {
    return modifiedPublicationNumber;
  }

  /**
   * @param modifiedPublicationNumber the modifiedPublicationNumber to set
   */
  public void setModifiedPublicationNumber(Integer modifiedPublicationNumber) {
    this.modifiedPublicationNumber = modifiedPublicationNumber;
  }
}
