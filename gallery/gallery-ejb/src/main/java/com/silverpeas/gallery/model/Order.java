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
  private List rows; // List of OrderRow

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

  public List getRows() {
    return rows;
  }

  public void setRows(List rows) {
    this.rows = rows;
  }

}
