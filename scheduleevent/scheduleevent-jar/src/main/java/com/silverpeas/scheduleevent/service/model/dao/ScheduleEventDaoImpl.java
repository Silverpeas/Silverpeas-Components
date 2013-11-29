/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.scheduleevent.service.model.dao;

import com.silverpeas.annotation.Repository;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.scheduleevent.constant.ScheduleEventConstant;
import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

@Repository("scheduleEventDao")
@Transactional
public class ScheduleEventDaoImpl implements ScheduleEventDao {

  @PersistenceContext
  private EntityManager theEntityManager;

  private EntityManager getEntityManager() {
    return theEntityManager;
  }

  @Override
  public String createScheduleEvent(ScheduleEvent scheduleEvent) {
    getEntityManager().persist(scheduleEvent);
    return scheduleEvent.getId();
  }

  @Override
  public void deleteScheduleEvent(ScheduleEvent scheduleEvent) {
    EntityManager entityManager = getEntityManager();
    ScheduleEvent attachedEvent = entityManager.merge(scheduleEvent);

    for (Iterator<Response> iterator = attachedEvent.getResponses().iterator(); iterator.hasNext();) {
      Response response = iterator.next();
      iterator.remove();
      entityManager.remove(response);
    }
    entityManager.remove(attachedEvent);

    // delete related schedule event comments
    CommentServiceFactory
        .getFactory()
        .getCommentService()
        .deleteAllCommentsOnPublication(ScheduleEvent.getResourceType(),
            new CommentPK(scheduleEvent.getId().toString(), ScheduleEventConstant.TOOL_ID));
  }

  @Override
  public ScheduleEvent getScheduleEvent(String scheduleEventId) {
    return getEntityManager().find(ScheduleEvent.class, scheduleEventId);
  }

  @Override
  public Set<ScheduleEvent> listScheduleEventsByCreatorId(String userId) {
    TypedQuery<ScheduleEvent> query = getEntityManager().createNamedQuery("findByAuthor",
        ScheduleEvent.class);
    query.setParameter("authorId", Integer.valueOf(userId));
    List<ScheduleEvent> events = query.getResultList();
    return new HashSet<ScheduleEvent>(events);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ScheduleEvent> listScheduleEventsByContributorId(String userId) {
    TypedQuery<ScheduleEvent> query = getEntityManager().createNamedQuery("findByContributor",
        ScheduleEvent.class);
    query.setParameter("contributorId", Integer.valueOf(userId));
    List<ScheduleEvent> events = query.getResultList();
    return new HashSet<ScheduleEvent>(events);
  }

  @Override
  public void updateScheduleEvent(ScheduleEvent scheduleEvent) {
    getEntityManager().merge(scheduleEvent);
  }

  @Override
  public void purgeResponseScheduleEvent(ScheduleEvent scheduleEvent, int userId) {
    ScheduleEvent attachedEvent = getEntityManager().merge(scheduleEvent);
    for (Iterator<Response> iterator = attachedEvent.getResponses().iterator(); iterator.hasNext();) {
      Response response = iterator.next();
      if (response.getUserId() == userId) {
        iterator.remove();
        getEntityManager().remove(response);
      }
    }
  }

  @Override
  public Contributor getContributor(String contributorId) {
    return getEntityManager().find(Contributor.class, contributorId);
  }

  @Override
  public void deleteContributor(Contributor contributor) {
    EntityManager entityManager = getEntityManager();
    Contributor attachedContributor = entityManager.merge(contributor);
    entityManager.remove(attachedContributor);
  }
}
