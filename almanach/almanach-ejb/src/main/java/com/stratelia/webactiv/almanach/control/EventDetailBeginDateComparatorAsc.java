/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.control;

import java.util.Calendar;
import java.util.Comparator;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.util.DateUtil;

public class EventDetailBeginDateComparatorAsc implements Comparator<EventDetail> {
  static public EventDetailBeginDateComparatorAsc comparator = new EventDetailBeginDateComparatorAsc();

  public int compare(EventDetail e1, EventDetail e2) {
    int hour = -1;
    int minutes = -1;

    Calendar date1 = Calendar.getInstance();
    date1.setTime(e1.getStartDate());

    String startHour = e1.getStartHour(); // 12:30
    if (StringUtil.isDefined(startHour)) {
      hour = DateUtil.extractHour(startHour);
      minutes = DateUtil.extractMinutes(startHour);
      date1.set(Calendar.HOUR_OF_DAY, hour);
      date1.set(Calendar.MINUTE, minutes);
    }

    Calendar date2 = Calendar.getInstance();
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
