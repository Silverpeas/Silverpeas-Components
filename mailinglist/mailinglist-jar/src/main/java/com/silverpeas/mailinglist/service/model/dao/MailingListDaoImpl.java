/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.mailinglist.service.model.dao;

import com.silverpeas.annotation.Repository;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

@Repository("mailingListDao")
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
    TypedQuery<MailingList> query = getEntityManager().createNamedQuery("findByComponentId",
        MailingList.class);
    query.setParameter("componentId", componentId);
    MailingList result = null;
    try {
      result = query.getSingleResult();
    } catch (NoResultException ex) {
      Logger.getLogger(getClass().getSimpleName()).log(Level.FINER, ex.getMessage());
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
        createNamedQuery("findAll", MailingList.class);
    return query.getResultList();
  }

  @Override
  public void updateMailingList(MailingList mailingList) {
    getEntityManager().merge(mailingList);
  }
}
