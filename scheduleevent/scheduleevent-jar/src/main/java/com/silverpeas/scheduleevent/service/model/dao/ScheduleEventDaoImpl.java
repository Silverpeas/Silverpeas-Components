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

package com.silverpeas.scheduleevent.service.model.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;

public class ScheduleEventDaoImpl extends HibernateDaoSupport implements ScheduleEventDao {

  public String createScheduleEvent(ScheduleEvent scheduleEvent) {
    String id = (String) getSession().save(scheduleEvent);
    scheduleEvent.setId(id);
    return id;
  }

  public void deleteScheduleEvent(ScheduleEvent scheduleEvent) {
    // purge all the response
    Set<Response> responses = scheduleEvent.getResponses();

    Response[] responsesArray = responses.toArray(new Response[responses.size()]);
    for (int i = 0; i < responsesArray.length; i++) {
      Response resp = responsesArray[i];
      responses.remove(resp);
      getSession().delete(resp);
      getSession().flush();
    }
    getSession().delete(scheduleEvent);
  }

  public ScheduleEvent getScheduleEventComplete(String scheduleEventId) {
	    Criteria criteria = getSession().createCriteria(ScheduleEvent.class);
	    criteria.add(Restrictions.eq("id", scheduleEventId));
	    return (ScheduleEvent) criteria.uniqueResult();
//	  return (ScheduleEvent) getSession().load(ScheduleEvent.class, scheduleEventId);
  }

  public Set<ScheduleEvent> listScheduleEventsByCreatorId(String userId) {
    Criteria criteria = getSession().createCriteria(ScheduleEvent.class);
    criteria.add(Restrictions.eq("author", Integer.parseInt(userId)));
    Set<ScheduleEvent> returnSet = new HashSet<ScheduleEvent>();
    returnSet.addAll(criteria.list());
    return returnSet;
  }

  public Set<ScheduleEvent> listScheduleEventsByContributorId(String userId) {
    Criteria criteria = getSession().createCriteria(Contributor.class);
    criteria.add(Restrictions.eq("userId", Integer.parseInt(userId)));
    Set<Contributor> returnSet = new HashSet<Contributor>();
    returnSet.addAll(criteria.list());
    
    Set<ScheduleEvent> scheduleEvents = new HashSet<ScheduleEvent>();
    if (returnSet != null && returnSet.size() > 0) {
      Iterator<Contributor> iterRes = returnSet.iterator();
      while (iterRes.hasNext()) {
        Contributor resp = iterRes.next();
        scheduleEvents.add(getScheduleEventComplete(resp.getScheduleEvent().getId()));
      }
    }
    return scheduleEvents;
  }

  public void updateScheduleEvent(ScheduleEvent scheduleEvent) {
    getSession().update(scheduleEvent);
  }

  public void purgeResponseScheduleEvent(ScheduleEvent scheduleEvent, int userId) {
    Set<Response> responses = scheduleEvent.getResponses();
    // remove old values if exists
    Iterator<Response> iterRes = responses.iterator();
    while (iterRes.hasNext()) {
      Response resp = iterRes.next();
      if (resp.getUserId() == userId) {
        getSession().delete(resp);
        getSession().flush();
      }
    }
  }

  public Contributor getContributor(String contributorId) {
    Criteria criteria = getSession().createCriteria(Contributor.class);
    criteria.add(Restrictions.eq("id", contributorId));
    return (Contributor) criteria.uniqueResult();
  }
  
  public void deleteContributor(Contributor contributor) {
    getSession().delete(contributor);
  }
}
