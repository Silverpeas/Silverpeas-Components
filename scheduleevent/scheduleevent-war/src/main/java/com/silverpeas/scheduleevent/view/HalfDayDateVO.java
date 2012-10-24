package com.silverpeas.scheduleevent.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.silverpeas.scheduleevent.service.model.beans.DateOption;

public class HalfDayDateVO implements DateVO {
  private static final int MORNING_HOUR_LIMIT = 12;
  private static final Integer HALF_DAY_AS_TIME_SEQUENCE_SIZE = 2;
  
  private final DisableTime defaultMorning = new DisableTime(this);
  private final DisableTime defaultAfternoon = new DisableTime(this);

  private enum HalfDayPart {
    MORNING, AFTERNOON
  }

  private Date date;
  private TimeVO morning;
  private TimeVO afternoon;

  public HalfDayDateVO(Date date) throws Exception {
    this.date = date;
    setupMorningPart();
    setupAfternoonPart();
  }

  private void setupAfternoonPart() {
    defaultMorning.setPartOfDay(PartOfDayMorning.getInstance());
    morning = defaultMorning;
  }

  private void setupMorningPart() {
    defaultAfternoon.setPartOfDay(PartOfDayAfternoon.getInstance());
    afternoon = defaultAfternoon;
  }

  private List<TimeVO> createDayPartAsTimeSequence() {
    List<TimeVO> times = new ArrayList<TimeVO>(HALF_DAY_AS_TIME_SEQUENCE_SIZE);
    times.add(morning);
    times.add(afternoon);
    return times;
  }

  @Override
  public boolean hasSameDateAs(DateOption date) {
    if (date == null)
      return false;
    if (this.date == null)
      return false;
    return this.date.equals(date.getDay());
  }

  public void addTime(DateOption date) throws Exception {
    if (hasSameDateAs(date)) {
      HalfDayPart halfDayPart = getHalfDayPartOf(date);
      addUnassignedTime(date, halfDayPart);
    } else {
      throw new Exception("Can't add time on disaligned dates");
    }
  }

  private void addUnassignedTime(DateOption date, HalfDayPart halfDayPart) throws Exception {
    if (canAddTimeFor(halfDayPart)) {
      addTime(halfDayPart, date);
    } else {
      throw new Exception(halfDayPart + " part is already set for " + date.getDay());
    }
  }

  private void addTime(HalfDayPart halfDayPart, DateOption date) throws Exception {
    switch (halfDayPart) {
      case MORNING:
        morning = makeMorning(date);
        break;
      case AFTERNOON:
        afternoon = makeAfternoon(date);
        break;
      default:
        throw new Exception("addTime: " + halfDayPart + " not supported");
    }
  }

  private boolean canAddTimeFor(HalfDayPart halfDayPart) {
    switch (halfDayPart) {
      case MORNING:
        return morning == defaultMorning;
      case AFTERNOON:
        return afternoon == defaultAfternoon;
    }
    return false;
  }

  private HalfDayPart getHalfDayPartOf(DateOption date) {
    return date.getHour() > MORNING_HOUR_LIMIT ? HalfDayPart.AFTERNOON : HalfDayPart.MORNING;
  }

  private HalfDayTime makeAfternoon(DateOption date) {
    HalfDayTime afternoon = new HalfDayTime(this, date);
    afternoon.setPartOfDay(PartOfDayAfternoon.getInstance());
    return afternoon;
  }

  private HalfDayTime makeMorning(DateOption date) {
    HalfDayTime morning = new HalfDayTime(this, date);
    morning.setPartOfDay(PartOfDayMorning.getInstance());
    return morning;
  }

  @Override
  public Date getDate() {
    return date;
  }

  @Override
  public List<TimeVO> getTimes() {
    List<TimeVO> times = createDayPartAsTimeSequence();
    return Collections.unmodifiableList(times);
  }

  @Override
  public Integer getTimesNumber() {
    return HALF_DAY_AS_TIME_SEQUENCE_SIZE;
  }

}
