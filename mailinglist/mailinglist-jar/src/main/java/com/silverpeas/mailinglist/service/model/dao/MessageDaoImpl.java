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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.mailinglist.service.model.dao;

import com.silverpeas.mailinglist.service.model.beans.Activity;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.silverpeas.util.cryptage.CryptMD5;
import com.stratelia.webactiv.util.exception.UtilException;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageDaoImpl extends HibernateDaoSupport implements MessageDao {

  public String saveMessage(Message message) {
    Message existingMessage = findMessageByMailId(message.getMessageId(),
        message.getComponentId());
    if (existingMessage == null) {
      if (message.getAttachments() != null
          && !message.getAttachments().isEmpty()) {
        for (Attachment attachment : message.getAttachments()) {
          saveAttachmentFile(attachment);
        }
      }
      String id = (String) getSession().save(message);
      message.setId(id);
      return id;
    }
    return existingMessage.getId();
  }

  public void updateMessage(Message message) {
    getSession().update(message);
  }

  public void deleteMessage(Message message) {
    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
      for (Attachment attachment : message.getAttachments()) {
        deleteAttachmentFile(attachment);
      }
    }
    getSession().delete(message);
  }

  public Message findMessageById(final String id) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("id", id));
    return (Message) criteria.uniqueResult();
  }

  public Message findMessageByMailId(final String messageId, String componentId) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.naturalId().set("componentId", componentId).set(
        "messageId", messageId));
    return (Message) criteria.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  public List<Message> listAllMessagesOfMailingList(final String componentId,
      final int page, final int elementsPerPage, final OrderBy orderBy) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.addOrder(orderBy.getOrder());
    int firstResult = page * elementsPerPage;
    criteria.setFirstResult(firstResult);
    criteria.setMaxResults(elementsPerPage);
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  public List<Message> listDisplayableMessagesOfMailingList(String componentId,
      final int month, final int year, final int page,
      final int elementsPerPage, final OrderBy orderBy) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.add(Restrictions.eq("moderated", Boolean.TRUE));
    if (month >= 0) {
      criteria.add(Restrictions.eq("month", month));
    }
    if (year >= 0) {
      criteria.add(Restrictions.eq("year", year));
    }
    criteria.addOrder(orderBy.getOrder());
    int firstResult = page * elementsPerPage;
    criteria.setFirstResult(firstResult);
    criteria.setMaxResults(elementsPerPage);
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  public List<Message> listUnmoderatedMessagesOfMailingList(String componentId,
      int page, int elementsPerPage, OrderBy orderBy) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.add(Restrictions.eq("moderated", Boolean.FALSE));
    criteria.addOrder(orderBy.getOrder());
    int firstResult = page * elementsPerPage;
    criteria.setFirstResult(firstResult);
    criteria.setMaxResults(elementsPerPage);
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  public List<Message> listActivityMessages(String componentId, int size, OrderBy orderBy) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.add(Restrictions.eq("moderated", Boolean.TRUE));
    criteria.addOrder(orderBy.getOrder());
    criteria.setMaxResults(size);
    return criteria.list();
  }

  public int listTotalNumberOfMessages(String componentId) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.setProjection(Projections.rowCount());
    return ((Long) criteria.uniqueResult()).intValue();
  }

  public int listTotalNumberOfDisplayableMessages(String componentId) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.add(Restrictions.eq("moderated", Boolean.TRUE));
    criteria.setProjection(Projections.rowCount());
    return ((Long) criteria.uniqueResult()).intValue();
  }

  public int listTotalNumberOfUnmoderatedMessages(String componentId) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.add(Restrictions.eq("moderated", Boolean.FALSE));
    criteria.setProjection(Projections.rowCount());
    return ((Long) criteria.uniqueResult()).intValue();
  }

  @SuppressWarnings("unchecked")
  public List<Activity> listActivity(String componentId) {
    Criteria criteria = getSession().createCriteria(Message.class);
    criteria.add(Restrictions.eq("componentId", componentId));
    criteria.add(Restrictions.eq("moderated", Boolean.TRUE));
    criteria.setProjection(Projections.projectionList().add(
        Projections.rowCount(), "nb").add(Projections.groupProperty("year"),
        "year").add(Projections.groupProperty("month"), "month"));
    List result = criteria.list();
    List<Activity> activities;
    if (result != null && !result.isEmpty()) {
      activities = new ArrayList<Activity>(result.size());
      for (Object aResult : result) {
        Object[] line = (Object[]) aResult;
        Activity activity = new Activity();
        activity.setNbMessages(((Long) line[0]).intValue());
        activity.setYear((Integer) line[1]);
        activity.setMonth((Integer) line[2]);
        activities.add(activity);
      }
    } else {
      activities = new ArrayList<Activity>();
    }
    return activities;
  }

  protected void saveAttachmentFile(Attachment attachment) {
    try {
      File file = new File(attachment.getPath());
      if (file.exists() && file.isFile()) {
        attachment.setSize(file.length());
        String hash = CryptMD5.hash(file);
        attachment.setMd5Signature(hash);
        Attachment existingFile = findAlreadyExistingAttachment(hash, file
            .length(), attachment.getFileName(), null);
        if (existingFile != null
            && !existingFile.getPath().equals(attachment.getPath())) {
          attachment.setPath(existingFile.getPath());
          file.delete();
        }
      }
    } catch (UtilException e) {
      e.printStackTrace();
    }
  }

  protected void deleteAttachmentFile(Attachment attachment) {
    File file = new File(attachment.getPath());
    if (file.exists() && file.isFile()) {
      Attachment existingFile = findAlreadyExistingAttachment(attachment
          .getMd5Signature(), attachment.getSize(), attachment.getFileName(),
          attachment.getId());
      if (existingFile == null) {
        file.delete();
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Attachment findAlreadyExistingAttachment(final String md5Hash,
      final long size, final String fileName, final String attachmentId) {
    Criteria criteria = getSession().createCriteria(Attachment.class);
    criteria.add(Restrictions.eq("md5Signature", md5Hash));
    criteria.add(Restrictions.eq("size", size));
    criteria.add(Restrictions.eq("fileName", fileName));
    if (attachmentId != null) {
      criteria.add(Restrictions.not(Restrictions.eq("id", attachmentId)));
    }
    List<Attachment> result = criteria.list();
    if (result != null && !result.isEmpty()) {
      return result.iterator().next();
    }
    return null;
  }

}