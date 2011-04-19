/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.scheduleevent.service;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import com.silverpeas.scheduleevent.service.model.dao.ResponseDao;
import com.silverpeas.scheduleevent.service.model.dao.ScheduleEventDao;

public class ScheduleEventServiceImpl implements ScheduleEventService {
  public static final String COMPONENT_NAME = "scheduleEvent";

  private ScheduleEventDao scheduleEventDao;
  private ResponseDao responseDao;

  public ScheduleEventDao getScheduleEventDao() {
    return scheduleEventDao;
  }

  public void setScheduleEventDao(ScheduleEventDao scheduleEventDao) {
    this.scheduleEventDao = scheduleEventDao;
  }

  public String createScheduleEvent(ScheduleEvent scheduleEvent) {
    return scheduleEventDao.createScheduleEvent(scheduleEvent);
  }

  public void deleteScheduleEvent(String scheduleEventId) {
    ScheduleEvent event = scheduleEventDao.getScheduleEventComplete(scheduleEventId);
    scheduleEventDao.deleteScheduleEvent(event);
  }

  public ScheduleEvent findScheduleEvent(String scheduleEventId) {
    return scheduleEventDao.getScheduleEventComplete(scheduleEventId);
  }

  public Set<ScheduleEvent> listAllScheduleEventsByUserId(String userId) {
    Set<ScheduleEvent> events = scheduleEventDao.listScheduleEventsByCreatorId(userId);
    events.addAll(scheduleEventDao.listScheduleEventsByContributorId(userId));
    return events;
  }

  public void updateScheduleEventStatus(String scheduleEventId, int newStatus) {
    ScheduleEvent event = scheduleEventDao.getScheduleEventComplete(scheduleEventId);
    event.setStatus(newStatus);
    scheduleEventDao.updateScheduleEvent(event);
  }

  public ScheduleEvent purgeOldResponseForUserId(ScheduleEvent scheduleEvent, int userId) {
    scheduleEventDao.purgeResponseScheduleEvent(scheduleEvent, userId);
    return scheduleEventDao.getScheduleEventComplete(scheduleEvent.getId());
  }

  public void updateScheduleEvent(ScheduleEvent scheduleEvent) {
    scheduleEventDao.updateScheduleEvent(scheduleEvent);
  }

  public void setResponseDao(ResponseDao responseDao) {
    this.responseDao = responseDao;
  }

  public ResponseDao getResponseDao() {
    return responseDao;
  }

  public void setLastVisited(String scheduleEventId, int userId) {
    ScheduleEvent event = scheduleEventDao.getScheduleEventComplete(scheduleEventId);
    SortedSet<Contributor> contributors = event.getContributors();
    Iterator<Contributor> iter = contributors.iterator();
    boolean finish = false;
    while (iter.hasNext() && !finish) {
      Contributor contrib = (Contributor) iter.next();
      if (userId == contrib.getUserId()) {
        contrib.setLastVisit(new Date());
        finish = true;
      }
    }
    scheduleEventDao.updateScheduleEvent(event);
  }

}
