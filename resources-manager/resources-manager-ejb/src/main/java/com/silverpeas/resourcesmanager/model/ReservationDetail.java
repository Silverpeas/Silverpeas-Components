/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ReservationDetail implements Serializable, ResourceStatus {
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

  @Override
  public String toString() {
    return "ReservationDetail{" + "id=" + id + ", event=" + event + ", beginDate=" + beginDate 
            + ", endDate=" + endDate + ", reason=" + reason + ", place=" + place + ", userId=" 
            + userId + ", creationDate=" + creationDate + ", updateDate=" + updateDate 
            + ", instanceId=" + instanceId + ", listResourcesReserved=" + listResourcesReserved 
            + ", status=" + status + ", userName=" + userName + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ReservationDetail other = (ReservationDetail) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if ((this.event == null) ? (other.event != null) : !this.event.equals(other.event)) {
      return false;
    }
    if (this.beginDate != other.beginDate && (this.beginDate == null || !this.beginDate.equals(
            other.beginDate))) {
      return false;
    }
    if (this.endDate != other.endDate && (this.endDate == null || !this.endDate.equals(other.endDate))) {
      return false;
    }
    if ((this.reason == null) ? (other.reason != null) : !this.reason.equals(other.reason)) {
      return false;
    }
    if ((this.place == null) ? (other.place != null) : !this.place.equals(other.place)) {
      return false;
    }
    if ((this.userId == null) ? (other.userId != null) : !this.userId.equals(other.userId)) {
      return false;
    }
    if (this.creationDate != other.creationDate && (this.creationDate == null || !this.creationDate.
            equals(other.creationDate))) {
      return false;
    }
    if (this.updateDate != other.updateDate && (this.updateDate == null || !this.updateDate.equals(
            other.updateDate))) {
      return false;
    }
    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId.equals(
            other.instanceId)) {
      return false;
    }
    if ((this.status == null) ? (other.status != null) : !this.status.equals(other.status)) {
      return false;
    }
    return !((this.userName == null) ? (other.userName != null) :
        !this.userName.equals(other.userName));
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 97 * hash + (this.event != null ? this.event.hashCode() : 0);
    hash = 97 * hash + (this.beginDate != null ? this.beginDate.hashCode() : 0);
    hash = 97 * hash + (this.endDate != null ? this.endDate.hashCode() : 0);
    hash = 97 * hash + (this.reason != null ? this.reason.hashCode() : 0);
    hash = 97 * hash + (this.place != null ? this.place.hashCode() : 0);
    hash = 97 * hash + (this.userId != null ? this.userId.hashCode() : 0);
    hash = 97 * hash + (this.creationDate != null ? this.creationDate.hashCode() : 0);
    hash = 97 * hash + (this.updateDate != null ? this.updateDate.hashCode() : 0);
    hash = 97 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    hash = 97 * hash + (this.status != null ? this.status.hashCode() : 0);
    hash = 97 * hash + (this.userName != null ? this.userName.hashCode() : 0);
    return hash;
  }
  
  public boolean isValidated() {
    return STATUS_VALIDATE.equals(status);
  }
  
  public boolean isRefused() {
    return STATUS_REFUSED.equals(status);
  }
  
  public boolean isValidationRequired() {
    return STATUS_FOR_VALIDATION.equals(status);
  }
}
