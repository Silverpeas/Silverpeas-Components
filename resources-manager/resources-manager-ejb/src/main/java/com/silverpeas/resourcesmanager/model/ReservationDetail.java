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
package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ReservationDetail implements Serializable {

  private static final long serialVersionUID = 1L;
  private String id;
  private String event;
  private Date beginDate;
  private Date endDate;
  private String reason;
  private String place;
  private String userId;
  private Date creationDate;
  private Date updateDate;
  private String instanceId;
  private List<ResourceDetail> listResourcesReserved;
  private String status;

  private String userName;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public List<ResourceDetail> getListResourcesReserved() {
    return listResourcesReserved;
  }

  public void setListResourcesReserved(List<ResourceDetail> listResourcesReserved) {
    this.listResourcesReserved = listResourcesReserved;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(Date beginDate) {
    this.beginDate = beginDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public String getPlace() {
    return place;
  }

  public void setPlace(String place) {
    this.place = place;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public ReservationDetail(String event, Date beginDate, Date endDate,
      String reason, String place) {
    super();
    this.event = event;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.reason = reason;
    this.place = place;
  }

  public ReservationDetail(String id, String event, Date beginDate,
      Date endDate, String reason, String place, String userId,
      Date creationDate, Date updateDate, String instanceId) {
    super();
    this.id = id;
    this.event = event;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.reason = reason;
    this.place = place;
    this.userId = userId;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.instanceId = instanceId;
  }

  public ReservationDetail(String id, String event, Date beginDate,
      Date endDate, String reason, String place, String userId,
      Date creationDate, Date updateDate, String instanceId, String status) {
    super();
    this.id = id;
    this.event = event;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.reason = reason;
    this.place = place;
    this.userId = userId;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.instanceId = instanceId;
    this.status = status;
  }

}
