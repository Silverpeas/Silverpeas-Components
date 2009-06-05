package com.silverpeas.mailinglist.service.model.beans;

public class Activity implements Comparable<Activity> {
  private int month;
  private int year;
  private int nbMessages;

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getNbMessages() {
    return nbMessages;
  }

  public void setNbMessages(int nbMessages) {
    this.nbMessages = nbMessages;
  }

  public int compareTo(Activity other) {
    if (other == null) {
      return -1;
    }
    if (this.equals(other)) {
      return 0;
    }
    if (year == other.getYear()) {
      return month - other.getMonth();
    }
    return year - other.getYear();
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    Activity other = (Activity) o;
    return (year == other.getYear() && month == other.getMonth() && nbMessages == other
        .getNbMessages());
  }
}
