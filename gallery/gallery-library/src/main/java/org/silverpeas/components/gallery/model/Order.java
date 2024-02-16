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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.model;

import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.Date;
import java.util.List;

public class Order {
  private String orderId;
  private UserDetail orderer;
  private String userId;
  private String instanceId;
  private Date creationDate;
  private String processUserId;
  private Date processDate;
  private List<OrderRow> rows; // List of OrderRow

  public Order(String userId, String instanceId, Date creationDate) {
    setUserId(userId);
    setInstanceId(instanceId);
    setCreationDate(creationDate);
  }

  public Order(String orderId) {
    setOrderId(orderId);
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getUserId() {
    return userId;
  }

  public UserDetail getOrderer() {
    if (StringUtil.isDefined(getUserId())) {
      if (orderer == null || !getUserId().equals(orderer.getId())) {
        orderer = UserDetail.getById(getUserId());
      }
    } else {
      orderer = null;
    }
    return orderer;
  }

  public void setOrderer(UserDetail orderer) {
    this.orderer = orderer;
    setUserId((orderer != null) ? orderer.getId() : null);
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getUserName() {
    return getOrderer() != null ? getOrderer().getDisplayedName() : null;
  }

  public int getNbRows() {
    return rows.size();
  }

  public boolean isProcessed() {
    return (processDate != null);
  }

  public String getProcessUserId() {
    return processUserId;
  }

  public void setProcessUserId(String processUserId) {
    this.processUserId = processUserId;
  }

  public Date getProcessDate() {
    return processDate;
  }

  public void setProcessDate(Date processDate) {
    this.processDate = processDate;
  }

  public List<OrderRow> getRows() {
    return rows;
  }

  public void setRows(List<OrderRow> rows) {
    this.rows = rows;
  }

}
