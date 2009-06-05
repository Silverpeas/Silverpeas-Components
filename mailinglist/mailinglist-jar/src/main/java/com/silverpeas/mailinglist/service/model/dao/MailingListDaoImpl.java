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
