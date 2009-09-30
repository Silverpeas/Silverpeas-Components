package com.silverpeas.gallery.model;

import java.util.Date;

public class MetaData {
  private String property;
  private String label;
  private boolean date = false;
  private String value; // value as String
  private Date dateValue; // value as Date if metadata is a date

  public MetaData() {
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isDate() {
    return date;
  }

  public void setDate(boolean date) {
    this.date = date;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public Date getDateValue() {
    return dateValue;
  }

  public void setDateValue(Date dateValue) {
    this.dateValue = dateValue;
  }
}
