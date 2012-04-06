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
 * "http://www.silverpeas.org/legal/licensing"
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

public class MostInterestedQueryVO implements Serializable {

  /**
   * Serializable purpose
   */
  private static final long serialVersionUID = 311256678113676898L;

  /**
   * The searched key words
   */
  private String query;

  /**
   * Number of occurences
   */
  private Integer occurrences;

  /**
   * @param query
   * @param occurrences
   */
  public MostInterestedQueryVO(String query, Integer occurrences) {
    this.query = query;
    this.occurrences = occurrences;
  }

  /**
   * @return the query
   */
  public String getQuery() {
    return query;
  }

  /**
   * @param query the query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * @return the occurrences
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * @param occurrences the occurrences to set
   */
  public void setOccurrences(Integer occurrences) {
    this.occurrences = occurrences;
  }

}
