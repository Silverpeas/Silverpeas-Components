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

import java.util.Date;

/**
 * This class aims to centralize statistic filter parameters. Mandatory parameter are given with the
 * StatsFilterVO default constructor.
 */
public class StatsFilterVO {

  /**
   * the application identifier
   */
  private String instanceId;
  /**
   * the topic identifier
   */
  private Integer topicId;
  /**
   * the start time
   */
  private Date startDate;
  /**
   * the end time
   */
  private Date endDate;
  /**
   * the group identifier
   */
  private Integer groupId;

  /**
   * @param instanceId
   * @param topicId
   * @param startDate
   * @param endDate
   */
  public StatsFilterVO(String instanceId, Integer topicId, Date startDate, Date endDate) {
    super();
    this.instanceId = instanceId;
    this.topicId = topicId;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * @return the instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId the instanceId to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * @return the topicId
   */
  public Integer getTopicId() {
    return topicId;
  }

  /**
   * @param topicId the topicId to set
   */
  public void setTopicId(Integer topicId) {
    this.topicId = topicId;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * @return the endDate
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the groupId
   */
  public Integer getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

}
