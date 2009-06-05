package com.silverpeas.mailinglist.service.util;

import org.hibernate.criterion.Order;

public class OrderBy {
  private String propertyName;
  private boolean asc;

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }

  public boolean isAsc() {
    return asc;
  }

  public void setAsc(final boolean asc) {
    this.asc = asc;
  }
  
  public Order getOrder() {
    if(asc){
      return Order.asc(propertyName);
    }
    return Order.desc(propertyName);
  }

  public OrderBy(final String propertyName, final boolean asc) {
    super();
    this.propertyName = propertyName;
    this.asc = asc;
  }
}
