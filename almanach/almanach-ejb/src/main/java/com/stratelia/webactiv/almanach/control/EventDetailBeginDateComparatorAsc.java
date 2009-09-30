package com.stratelia.webactiv.almanach.control;

import java.util.Calendar;
import java.util.Comparator;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.util.DateUtil;

public class EventDetailBeginDateComparatorAsc implements Comparator {
  static public EventDetailBeginDateComparatorAsc comparator = new EventDetailBeginDateComparatorAsc();

  public int compare(Object o1, Object o2) {
    int hour = -1;
    int minutes = -1;

    Calendar date1 = Calendar.getInstance();
    EventDetail e1 = (EventDetail) o1;
    date1.setTime(e1.getStartDate());

    String startHour = e1.getStartHour(); // 12:30
    if (StringUtil.isDefined(startHour)) {
      hour = DateUtil.extractHour(startHour);
      minutes = DateUtil.extractMinutes(startHour);
      date1.set(Calendar.HOUR_OF_DAY, hour);
      date1.set(Calendar.MINUTE, minutes);
    }

    Calendar date2 = Calendar.getInstance();
    EventDetail e2 = (EventDetail) o2;
    date2.setTime(e2.getStartDate());
    startHour = e2.getStartHour();
    if (StringUtil.isDefined(startHour)) {
      hour = DateUtil.extractHour(startHour);
      minutes = DateUtil.extractMinutes(startHour);
      date2.set(Calendar.HOUR_OF_DAY, hour);
      date2.set(Calendar.MINUTE, minutes);
    }

    int compareResult = (new Long(date1.getTimeInMillis())).compareTo(new Long(
        (date2.getTimeInMillis())));

    return compareResult;
  }

  public boolean equals(Object o) {
    return o == this;
  }
}
