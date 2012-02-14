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
package org.silverpeas.resourcemanager.model;

import com.silverpeas.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import java.util.Date;

@Entity
@Table(name = "sc_resources_reservation")
public class Reservation implements ResourceStatus {

  private static final long serialVersionUID = -1410243488372828193L;
  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
  valueColumnName = "maxId", pkColumnValue = "sc_resources_reservation")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  private Long id;
  @Column(name = "evenement", length = 128, nullable = false)
  private String event;
  @Column(length = 20, nullable = false)
  private String beginDate;
  @Column(length = 20, nullable = false)
  private String endDate;
  @Column(length = 2000, nullable = true)
  private String reason;
  @Column(length = 128, nullable = true)
  private String place;
  @Column
  private int userId;
  @Column(length = 20, nullable = false)
  private String creationDate;
  @Column(length = 20, nullable = false)
  private String updateDate;
  @Column
  private String instanceId;
  @Column
  private String status;
  @Transient
  private String userName;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Date getCreationDate() {
    if (StringUtil.isLong(creationDate)) {
      Date creation = new Date();
      creation.setTime(Long.parseLong(creationDate));
      return creation;
    }
    return null;
  }

  public void setCreationDate(Date creationDate) {
    if (creationDate != null) {
      this.creationDate = String.valueOf(creationDate.getTime());
    } else {
      this.creationDate = null;
    }
  }

  public String getId() {
    return String.valueOf(id);
  }

  public Long getIntegerId() {
    return id;
  }

  public final void setId(String id) {
    if (StringUtil.isLong(id)) {
      this.id = Long.parseLong(id);
    }
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Date getUpdateDate() {
    if (StringUtil.isLong(updateDate)) {
      Date update = new Date();
      update.setTime(Long.parseLong(updateDate));
      return update;
    }
    return null;
  }

  public void setUpdateDate(Date updateDate) {
    if (updateDate != null) {
      this.updateDate = String.valueOf(updateDate.getTime());
    }
  }

  public String getUserId() {
    return String.valueOf(userId);
  }

  public void setUserId(String userId) {
    if (StringUtil.isInteger(userId)) {
      this.userId = Integer.parseInt(userId);
    }
  }

  public Date getBeginDate() {
    if (StringUtil.isLong(beginDate)) {
      Date begin = new Date();
      begin.setTime(Long.parseLong(beginDate));
      return begin;
    }
    return null;
  }

  public void setBeginDate(Date beginDate) {
    if (beginDate != null) {
      this.beginDate = String.valueOf(beginDate.getTime());
    }
  }

  public Date getEndDate() {
    if (StringUtil.isLong(endDate)) {
      Date end = new Date();
      end.setTime(Long.parseLong(endDate));
      return end;
    }
    return null;
  }

  public void setEndDate(Date endDate) {
    if (endDate != null) {
      this.endDate = String.valueOf(endDate.getTime());
    }
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

  public Reservation() {
  }

  public Reservation(String event, Date beginDate, Date endDate,
      String reason, String place) {
    this.event = event;
    setBeginDate(beginDate);
    setEndDate(endDate);
    this.reason = reason;
    this.place = place;
  }

  public Reservation(String id, String event, Date beginDate,
      Date endDate, String reason, String place, String userId,
      Date creationDate, Date updateDate, String instanceId) {
    setId(id);
    this.event = event;
    setBeginDate(beginDate);
    setEndDate(endDate);
    this.reason = reason;
    this.place = place;
    setUserId(userId);
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    this.instanceId = instanceId;
  }

  public Reservation(String id, String event, Date beginDate,
      Date endDate, String reason, String place, String userId,
      Date creationDate, Date updateDate, String instanceId, String status) {
    setId(id);
    this.event = event;
    setBeginDate(beginDate);
    setEndDate(endDate);
    this.reason = reason;
    this.place = place;
    setUserId(userId);
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    this.instanceId = instanceId;
    this.status = status;
  }
  
  public void merge(Reservation reservation) {
    this.beginDate = reservation.beginDate;
    this.creationDate = reservation.creationDate;
    this.endDate = reservation.endDate;
    this.event = reservation.event;
  }

  @Override
  public String toString() {
    return "Reservation{" + "id=" + id + ", event=" + event + ", beginDate=" + beginDate +
        ", endDate=" + endDate + ", reason=" + reason + ", place=" + place + ", userId=" +
        userId + ", creationDate=" + creationDate + ", updateDate=" + updateDate +
        ", instanceId=" + instanceId + ", status=" + status + ", userName=" + userName + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Reservation other = (Reservation) obj;
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
    if (this.userId != (other.userId)) {
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
    return !((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId.equals(
        other.instanceId));
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
    hash = 97 * hash + this.userId;
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
