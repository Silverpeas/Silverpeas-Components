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
package com.silverpeas.mailinglist.service.model.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.silverpeas.mailinglist.service.model.beans.MailingList;

public class MailingListDaoImpl extends HibernateDaoSupport implements
    MailingListDao {

  public String createMailingList(MailingList mailingList) {
    String id = (String) getSession().save(mailingList);
    mailingList.setId(id);
    return id;
  }

  public void deleteMailingList(MailingList mailingList) {
    getSession().delete(mailingList);
  }

  public MailingList findByComponentId(String componentId) {
    Criteria criteria = getSession().createCriteria(MailingList.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    return (MailingList) criteria.uniqueResult();
  }

  public MailingList findById(String id) {
    Criteria criteria = getSession().createCriteria(MailingList.class);
    criteria.add(Restrictions.eq("id", id));
    return (MailingList) criteria.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  public List<MailingList> listMailingLists() {
    Criteria criteria = getSession().createCriteria(MailingList.class);
    return criteria.list();
  }

  public void updateMailingList(MailingList mailingList) {
    getSession().update(mailingList);
  }

}
