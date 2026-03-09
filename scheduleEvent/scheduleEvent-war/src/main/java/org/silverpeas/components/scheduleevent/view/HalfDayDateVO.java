package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.kernel.SilverpeasException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HalfDayDateVO implements DateVO {
  private static final int MORNING_HOUR_LIMIT = 12;
  private static final Integer HALF_DAY_AS_TIME_SEQUENCE_SIZE = 2;

  private final DisableTime defaultMorning = new DisableTime(this);
  private final DisableTime defaultAfternoon = new DisableTime(this);

  private enum HalfDayPart {
    MORNING, AFTERNOON
  }

  private final Date date;
  private TimeVO morning;
  private TimeVO afternoon;

  public HalfDayDateVO(Date date) {
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
    List<TimeVO> times = new ArrayList<>(HALF_DAY_AS_TIME_SEQUENCE_SIZE);
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

  public void addTime(DateOption date) throws SilverpeasException {
    if (hasSameDateAs(date)) {
      HalfDayPart halfDayPart = getHalfDayPartOf(date);
      addUnassignedTime(date, halfDayPart);
    } else {
      throw new SilverpeasException("Can't add time on disaligned dates");
    }
  }

  private void addUnassignedTime(DateOption date, HalfDayPart halfDayPart) throws SilverpeasException {
    if (canAddTimeFor(halfDayPart)) {
      addTime(halfDayPart, date);
    } else {
      throw new SilverpeasException(halfDayPart + " part is already set for " + date.getDay());
    }
  }

  private void addTime(HalfDayPart halfDayPart, DateOption date) throws SilverpeasException {
    switch (halfDayPart) {
      case MORNING:
        morning = makeMorning(date);
        break;
      case AFTERNOON:
        afternoon = makeAfternoon(date);
        break;
      default:
        throw new SilverpeasException("addTime: " + halfDayPart + " not supported");
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
    HalfDayTime theAfternoon = new HalfDayTime(this, date);
    theAfternoon.setPartOfDay(PartOfDayAfternoon.getInstance());
    return theAfternoon;
  }

  private HalfDayTime makeMorning(DateOption date) {
    HalfDayTime theMorning = new HalfDayTime(this, date);
    theMorning.setPartOfDay(PartOfDayMorning.getInstance());
    return theMorning;
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
