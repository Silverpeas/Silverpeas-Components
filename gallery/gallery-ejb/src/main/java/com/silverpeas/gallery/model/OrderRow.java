package com.silverpeas.gallery.model;

import java.util.Date;

public class OrderRow {
  private int orderId;
  private int photoId;
  private PhotoDetail photo;
  private String instanceId;
  private Date downloadDate;
  private String downloadDecision;

  public OrderRow(int orderId, int photoId, String instanceId) {
    setOrderId(orderId);
    setPhotoId(photoId);
    setInstanceId(instanceId);
  }

  public int getOrderId() {
    return orderId;
  }

  public void setOrderId(int orderId) {
    this.orderId = orderId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int getPhotoId() {
    return photoId;
  }

  public void setPhotoId(int photoId) {
    this.photoId = photoId;
  }

  public Date getDownloadDate() {
    return downloadDate;
  }

  public void setDownloadDate(Date downloadDate) {
    this.downloadDate = downloadDate;
  }

  public String getDownloadDecision() {
    return downloadDecision;
  }

  public void setDownloadDecision(String downloadDecision) {
    this.downloadDecision = downloadDecision;
  }

  public PhotoDetail getPhoto() {
    return photo;
  }

  public void setPhoto(PhotoDetail photo) {
    this.photo = photo;
  }

}
