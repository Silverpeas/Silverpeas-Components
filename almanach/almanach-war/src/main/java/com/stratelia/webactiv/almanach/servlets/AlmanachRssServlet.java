/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.servlets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import com.silverpeas.peasUtil.RssServlet;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class AlmanachRssServlet extends RssServlet<EventDetail> {
  
  private static final long serialVersionUID = -2142983612465351228L;

  /*
   * (non-Javadoc)
   *
   * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String,
   * int)
   */
  public Collection<EventDetail> getListElements(String instanceId, int nbReturned)
      throws RemoteException {
    // récupération de la liste des 10 prochains événements de l'Almanach
    Collection<EventDetail> result = new ArrayList<EventDetail>();

    Collection<EventDetail> allEvents = getAlmanachBm().getAllEvents(
        new EventPK("", "", instanceId));
    net.fortuna.ical4j.model.Calendar calendarAlmanach = getAlmanachBm()
        .getICal4jCalendar(allEvents, "fr");

    Calendar currentDay = GregorianCalendar.getInstance();
    Collection<EventDetail> events = getAlmanachBm().getListRecurrentEvent(calendarAlmanach,
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
   * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object,
   * java.lang.String)
   */
  public String getElementTitle(EventDetail event, String userId) {
    return event.getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object,
   * java.lang.String)
   */
  public String getElementLink(EventDetail event, String userId) {
    return event.getPermalink();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  public String getElementDescription(EventDetail event, String userId) {
    return event.getDescription();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
   */
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

  public String getElementCreatorId(EventDetail event) {
    return event.getCreatorId();
  }

  private AlmanachBm getAlmanachBm() {
    AlmanachBm almanachBm = null;
    try {
      AlmanachBmHome almanachBmHome = (AlmanachBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class);
      almanachBm = almanachBmHome.create();
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachRssServlet.getAlmanachBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return almanachBm;
  }
}