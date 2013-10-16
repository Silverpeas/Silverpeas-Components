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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.gallery.model;

import java.util.Date;
import java.util.List;

public class Order {
  private int orderId;
  private int userId;
  private String userName;
  private String instanceId;
  private Date creationDate;
  private int processUserId;
  private Date processDate;
  private List<OrderRow> rows; // List of OrderRow

  public Order(int userId, String instanceId, Date creationDate) {
    setUserId(userId);
    setInstanceId(instanceId);
    setCreationDate(creationDate);
  }

  public Order(int orderId) {
    setOrderId(orderId);
  }

  public int getOrderId() {
    return orderId;
  }

  public void setOrderId(int orderId) {
    this.orderId = orderId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
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
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getNbRows() {
    return rows.size();
  }

  public boolean isProcessed() {
    return (processDate != null);
  }

  public int getProcessUserId() {
    return processUserId;
  }

  public void setProcessUserId(int processUserId) {
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
