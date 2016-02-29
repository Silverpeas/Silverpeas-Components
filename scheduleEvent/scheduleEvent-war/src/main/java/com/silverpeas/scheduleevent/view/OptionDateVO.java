package com.silverpeas.scheduleevent.view;

import java.util.Date;

import com.silverpeas.scheduleevent.service.model.beans.DateOption;

public class OptionDateVO implements Comparable<OptionDateVO> {

  private static final int MORNING_HOUR_LIMIT = 12;
  private static final String MORNING_INDEX_MARKER = "AM";
  private static final String AFTERNOON_INDEX_MARKER = "PM";

  private boolean morning;
  private boolean afternoon;
  Date date;

  public OptionDateVO(Date date) {
    this.date = date;
    this.setMorning(false);
    this.setAfternoon(false);
  }

  public boolean isSameDateAs(DateOption dateOption) {
    if (dateOption == null)
      return false;
    if (date == null)
      return false;
    return date.equals(dateOption.getDay());
  }

  public void setPartOfDayFromHour(DateOption dateOption) throws Exception {
    if (isSameDateAs(dateOption)) {
      setPartOfDayFromHour(dateOption.getHour());
    } else {
      throw new Exception("Cannot assign a part of day for two different dates");
    }
  }

  private void setPartOfDayFromHour(int hour) {
    // if (hour > MORNING_HOUR_LIMIT) {
      setAfternoon(true);
    // } else {
      setMorning(true);
    // }
  }

  public Date getDate() {
    return date;
  }

  public String getMorningIndexFormat() {
    return MORNING_INDEX_MARKER + getIndexFormat();
  }

  public String getAfternoonIndexFormat() {
    return AFTERNOON_INDEX_MARKER + getIndexFormat();
  }

  public String getIndexFormat() {
    return OptionalDateFormatterForIndex.format(date);
  }

  public void setAfternoon(boolean afternoon) {
    this.afternoon = afternoon;
  }

  public boolean isAfternoon() {
    return afternoon;
  }

  public void setMorning(boolean morning) {
    this.morning = morning;
  }

  public boolean isMorning() {
    return morning;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OptionDateVO other = (OptionDateVO) obj;
    if (date == null) {
      if (other.date != null)
        return false;
    } else if (!date.equals(other.date))
      return false;
    return true;
  }

  @Override
  public int compareTo(OptionDateVO optionDate) {
    return getDate().compareTo(optionDate.getDate());
  }

}