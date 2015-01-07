/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.model;

import org.silverpeas.persistence.model.identifier.UniqueLongIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaCustomEntity;
import org.silverpeas.util.StringUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sc_resources_reservation")
@NamedQueries({@NamedQuery(name = "reservation.findAllReservationsInRange",
    query = "SELECT reservation FROM Reservation reservation " +
        "WHERE reservation.instanceId = :instanceId " +
        "AND reservation.beginDate < :endPeriod  AND reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservationsForUserInRange",
        query = "SELECT reservation FROM Reservation reservation " +
            "WHERE reservation.instanceId = :instanceId AND reservation.userId= :userId " +
            "AND reservation.beginDate < :endPeriod AND reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservationsForUser",
        query = "SELECT reservation FROM Reservation reservation " +
            "WHERE reservation.instanceId = :instanceId AND reservation.userId= :userId"),
    @NamedQuery(name = "reservation.findAllReservationsForValidation",
        query = "SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
            "JOIN reservedResource.resource.managers manager WHERE reservedResource.status = 'A' " +
            "AND manager.id.managerId = :managerId AND reservedResource.reservation.instanceId = :instanceId " +
            "AND reservedResource.reservation.beginDate < :endPeriod " +
            "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservationsNotRefusedForResourceInRange",
        query = "SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
            "WHERE reservedResource.resource.id = :resourceId AND reservedResource.status != 'R' " +
            "AND reservedResource.reservation.beginDate < :endPeriod " +
            "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservationsForCategoryInRange",
        query = "SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
            "WHERE reservedResource.resource.category.id = :categoryId " +
            "AND reservedResource.reservation.instanceId = :instanceId " +
            "AND reservedResource.reservation.beginDate < :endPeriod " +
            "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservationsForUserAndCategoryInRange",
        query = "SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
            "WHERE reservedResource.resource.category.id = :categoryId " +
            "AND reservedResource.reservation.instanceId = :instanceId AND reservedResource.reservation.userId = :userId " +
            "AND reservedResource.reservation.beginDate < :endPeriod " +
            "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservationsForResourceInRange",
        query ="SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
            "WHERE reservedResource.resource.id = :resourceId " +
            "AND reservedResource.reservation.instanceId = :instanceId " +
            "AND reservedResource.reservation.beginDate < :endPeriod " +
            "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservationsForUserAndResourceInRange",
        query = "SELECT DISTINCT reservedResource.reservation FROM ReservedResource reservedResource " +
            "WHERE reservedResource.resource.id = :resourceId " +
            "AND reservedResource.reservation.instanceId = :instanceId AND reservedResource.reservation.userId = :userId " +
            "AND reservedResource.reservation.beginDate < :endPeriod " +
            "AND reservedResource.reservation.endDate > :startPeriod "),
    @NamedQuery(name = "reservation.findAllReservations",
        query = "SELECT DISTINCT reservation FROM Reservation reservation WHERE reservation.instanceId = :instanceId")
})
public class Reservation extends AbstractJpaCustomEntity<Reservation, UniqueLongIdentifier>
    implements ResourceStatus {
  private static final long serialVersionUID = 4901854718854856161L;

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

  @Override
  public void performBeforePersist() {
    Date now = new Date();
    setCreationDate(now);
    setUpdateDate(now);
  }

  @Override
  public void performBeforeUpdate() {
    setUpdateDate(new Date());
  }

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

  public Long getIdAsLong() {
    return getNativeId().getId();
  }

  public String getIdAsString() {
    return getId();
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
    this.updateDate = String.valueOf(updateDate.getTime());
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

  public Reservation(String event, Date beginDate, Date endDate, String reason, String place) {
    this.event = event;
    setBeginDate(beginDate);
    setEndDate(endDate);
    this.reason = reason;
    this.place = place;
  }

  public Reservation(Long id, String event, Date beginDate, Date endDate, String reason,
      String place, String userId, Date creationDate, Date updateDate, String instanceId) {
    setId(Long.toString(id));
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

  public Reservation(Long id, String event, Date beginDate, Date endDate, String reason,
      String place, String userId, Date creationDate, Date updateDate, String instanceId,
      String status) {
    setId(Long.toString(id));
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

  @Override
  public String toString() {
    return "Reservation{" + "id=" + getId() + ", event=" + event + ", beginDate=" + beginDate +
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
    EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(getId(), other.getId());
    matcher.append(getEvent(), other.getEvent());
    matcher.append(getBeginDate(), other.getBeginDate());
    matcher.append(getEndDate(), other.getEndDate());
    matcher.append(getReason(), other.getReason());
    matcher.append(getPlace(), other.getPlace());
    matcher.append(getUserId(), other.getUserId());
    matcher.append(getInstanceId(), other.getInstanceId());
    return matcher.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getId());
    hash.append(getEvent());
    hash.append(getBeginDate());
    hash.append(getEndDate());
    hash.append(getReason());
    hash.append(getPlace());
    hash.append(getUserId());
    hash.append(getInstanceId());
    return hash.toHashCode();
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
