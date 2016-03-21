/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.almanach.servlets;

import org.silverpeas.core.web.util.servlet.RssServlet;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.util.DateUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class AlmanachRssServlet extends RssServlet<EventDetail> {

  private static final long serialVersionUID = -2142983612465351228L;

  @Inject
  private AlmanachService almanachService;

  /*
   * (non-Javadoc)
   *
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getListElements(java.lang.String,
   * int)
   */
  @Override
  public Collection<EventDetail> getListElements(String instanceId, int nbReturned) {
    // récupération de la liste des 10 prochains événements de l'Almanach
    Collection<EventDetail> result = new ArrayList<>();

    Collection<EventDetail> allEvents = getAlmanachService().getAllEvents(
        new EventPK("", "", instanceId));
    net.fortuna.ical4j.model.Calendar calendarAlmanach = getAlmanachService()
        .getICal4jCalendar(allEvents, "fr");

    Calendar currentDay = GregorianCalendar.getInstance();
    Collection<EventDetail> events = getAlmanachService().getListRecurrentEvent(calendarAlmanach,
        null, "", instanceId, true);
    if (events != null) {
      Iterator<EventDetail> it = events.iterator();
      EventDetail eventDetail;
      int nb = 1;
      while (nb <= nbReturned && it.hasNext()) {
        eventDetail = it.next();
        if (eventDetail.getStartDate().after(currentDay.getTime())) {
          result.add(eventDetail);
          nb++;
        }
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getElementTitle(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public String getElementTitle(EventDetail event, String userId) {
    return event.getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getElementLink(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public String getElementLink(EventDetail event, String userId) {
    return event.getPermalink();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.silverpeas.core.web.util.servlet.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public String getElementDescription(EventDetail event, String userId) {
    return event.getDescription();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getElementDate(java.lang.Object)
   */
  @Override
  public Date getElementDate(EventDetail event) {
    Calendar calElement = GregorianCalendar.getInstance();
    calElement.setTime(event.getStartDate());
    String hourMinute = event.getStartHour(); // hh:mm
    if (hourMinute != null && hourMinute.trim().length() > 0) {
      /*
       * int hour = new Integer(hourMinute.substring(0, 2)).intValue() - 1; //-1
       * car bug d'affichage du fil RSS qui affiche toujours 1h en trop
       */
      int hour = DateUtil.extractHour(hourMinute);
      int minute = DateUtil.extractMinutes(hourMinute);
      calElement.set(Calendar.HOUR_OF_DAY, hour);
      calElement.set(Calendar.MINUTE, minute);
    } else {
      /*
       * calElement.set(Calendar.HOUR_OF_DAY, -1);//-1 car bug d'affichage du
       * fil RSS qui affiche toujours 1h en trop
       */
      calElement.set(Calendar.HOUR_OF_DAY, 0);
      calElement.set(Calendar.MINUTE, 0);
    }
    return calElement.getTime();
  }

  @Override
  public String getElementCreatorId(EventDetail event) {
    return event.getCreatorId();
  }

  private AlmanachService getAlmanachService() {
    return almanachService;
  }
}