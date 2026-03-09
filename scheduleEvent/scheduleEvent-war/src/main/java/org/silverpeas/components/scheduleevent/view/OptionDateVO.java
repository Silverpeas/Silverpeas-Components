package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.kernel.SilverpeasException;

import java.util.Date;

public class OptionDateVO implements Comparable<OptionDateVO> {

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

  public void setPartOfDayFromHour(DateOption dateOption) throws SilverpeasException {
    if (isSameDateAs(dateOption)) {
      setPartOfDayFromHour();
    } else {
      throw new SilverpeasException("Cannot assign a part of day for two different dates");
    }
  }

  private void setPartOfDayFromHour() {
    setAfternoon(true);
    setMorning(true);
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
      return other.date == null;
    } else return date.equals(other.date);
  }

  @Override
  public int compareTo(OptionDateVO optionDate) {
    return getDate().compareTo(optionDate.getDate());
  }

}