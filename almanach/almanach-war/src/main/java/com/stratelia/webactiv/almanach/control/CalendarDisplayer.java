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

import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;

/**
 *
 * @author ehugonnet
 */
public class CalendarDisplayer {

  static final String NEW_LINE = System.getProperty("line.separator");

  /**
   * 
   * @param out
   * @param almanach
   * @param resources
   * @return
   * @throws AlmanachException
   * @throws RemoteException
   */
  public String displayCalendar(AlmanachSessionController almanach,
      ResourcesWrapper resources) throws AlmanachException, RemoteException {
    Collection<EventDetail> events = almanach.getListRecurrentEvent();
    int firstDayOfWeek = Integer.parseInt(resources.getString("GML.weekFirstDay"));
    StringBuilder buffer = new StringBuilder();
    buffer.append(displayCalendarHeader(resources, firstDayOfWeek));
    buffer.append("<tr>");
    buffer.append(NEW_LINE);
    buffer.append(fillFirstWeek(firstDayOfWeek));
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 1);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    int currentMonth = calendar.get(Calendar.MONTH);
    while (currentMonth == calendar.get(Calendar.MONTH)) {
      String style = "intfdcolor4";
      for (EventDetail event : events) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(event.getStartDate().getTime());
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        if (startDate.before(calendar)) {
          Calendar endDate = Calendar.getInstance();
          if (event.getEndDate() != null) {
            endDate.setTimeInMillis(event.getEndDate().getTime());
            endDate.set(Calendar.HOUR_OF_DAY, 0);
            endDate.set(Calendar.SECOND, 0);
            endDate.set(Calendar.MILLISECOND, 0);
          } else {
            endDate = startDate;
          }
          endDate.set(Calendar.MINUTE, 2);
          if (endDate.after(event)) {
            style = "ongletOff";
          }
        }
      }
      buffer.append("<td class=\"").append(style).append("\" align=\"center\">");
      buffer.append("<a href=\"javascript:onClick=selectDay('");
      buffer.append(resources.getInputDate(calendar.getTime())).append("')\" class=\"");
      buffer.append("chiffreCalendrier\">").append(calendar.get(Calendar.DAY_OF_MONTH));
      buffer.append("</a></td>");
      buffer.append(NEW_LINE);
      calendar.add(Calendar.DATE, 1);
      if (calendar.get(Calendar.DAY_OF_WEEK) == firstDayOfWeek) {
        buffer.append("</tr>").append(NEW_LINE);
        if (currentMonth == calendar.get(Calendar.MONTH)) {
          buffer.append("<tr>").append(NEW_LINE);
        }
      }
    }
    buffer.append(fillEndWeek(firstDayOfWeek));
    return buffer.toString();
  }

  protected String displayCalendarHeader(ResourcesWrapper resources, int firstDayOfWeek) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<tr>");
    buffer.append(NEW_LINE);
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
    for (int i = 0; i < 7; i++) {
      buffer.append("<td align=\"center\" class=\"ongletOff\">");
      buffer.append(resources.getString("GML.shortJour" + calendar.get(Calendar.DAY_OF_WEEK)));
      buffer.append("</td>");
      buffer.append(NEW_LINE);
      calendar.add(Calendar.DAY_OF_WEEK, 1);
    }
    buffer.append("</tr>");
    buffer.append(NEW_LINE);
    return buffer.toString();
  }

  protected String fillFirstWeek(int firstDayOfWeek) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    StringBuilder buffer = new StringBuilder();
    buffer.append("<tr>");
    buffer.append(NEW_LINE);
    while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
      buffer.append("<td align=\"center\" class=\"intfdcolor4\">&nbsp;</td>");
      buffer.append(NEW_LINE);
      calendar.add(Calendar.DAY_OF_WEEK, -1);
    }
    return buffer.toString();
  }

  protected String fillEndWeek(int firstDayOfWeek) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.add(Calendar.MONTH, +1);
    StringBuilder buffer = new StringBuilder();
    while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
      buffer.append("<td align=\"center\" class=\"intfdcolor4\">&nbsp;</td>");
      buffer.append(NEW_LINE);
      calendar.add(Calendar.DAY_OF_WEEK, 1);
    }
    buffer.append("</tr>");
    buffer.append(NEW_LINE);
    return buffer.toString();
  }
}
