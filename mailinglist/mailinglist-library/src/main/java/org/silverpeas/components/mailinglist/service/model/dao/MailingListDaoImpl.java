/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.mailinglist.service.model.dao;

import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Named("mailingListDAO")
@Transactional
public class MailingListDaoImpl implements MailingListDao {

  @PersistenceContext
  private EntityManager entityManager;

  private EntityManager getEntityManager() {
    return this.entityManager;
  }

  @Override
  public String createMailingList(MailingList mailingList) {
    getEntityManager().persist(mailingList);
    return mailingList.getId();
  }

  @Override
  public void deleteMailingList(MailingList mailingList) {
    EntityManager theEntityManager = getEntityManager();
    MailingList attachedMailingList = theEntityManager.merge(mailingList);
    theEntityManager.remove(attachedMailingList);
  }

  @Override
  public MailingList findByComponentId(String componentId) {
    TypedQuery<MailingList> query =
        getEntityManager().createNamedQuery("mailinglist.findByComponentId", MailingList.class);
    query.setParameter("componentId", componentId);
    MailingList result = null;
    try {
      result = query.getSingleResult();
    } catch (NoResultException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return result;
  }

  @Override
  public MailingList findById(String id) {
    return getEntityManager().find(MailingList.class, id);
  }

  @Override
  public List<MailingList> listMailingLists() {
    TypedQuery<MailingList> query = getEntityManager().
        createNamedQuery("mailinglist.findAll", MailingList.class);
    return query.getResultList();
  }

  @Override
  public void updateMailingList(MailingList mailingList) {
    getEntityManager().merge(mailingList);
  }
}
