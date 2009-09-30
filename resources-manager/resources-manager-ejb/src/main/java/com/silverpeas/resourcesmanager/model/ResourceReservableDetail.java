package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;

public class ResourceReservableDetail implements Serializable {

  String categoryId;
  String resourceId;
  String categoryName;
  String resourceName;

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public ResourceReservableDetail(String categoryId, String resourceId,
      String categoryName, String resourceName) {
    super();
    this.categoryId = categoryId;
    this.resourceId = resourceId;
    this.categoryName = categoryName;
    this.resourceName = resourceName;
  }

}
