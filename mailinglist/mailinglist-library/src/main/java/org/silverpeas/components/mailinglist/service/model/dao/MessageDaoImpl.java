/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.model.dao;

import org.silverpeas.components.mailinglist.service.model.beans.Activity;
import org.silverpeas.components.mailinglist.service.model.beans.Attachment;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.util.OrderBy;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.security.encryption.cipher.CryptMD5;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Repository
@Named("messageDao")
@Transactional
public class MessageDaoImpl implements MessageDao {

  private static final String COMPONENT_ID = "componentId";
  private static final String MODERATED = "moderated";
  @PersistenceContext
  private EntityManager entityManager;

  private EntityManager getEntityManager() {
    return this.entityManager;
  }

  @Override
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
      getEntityManager().persist(message);
      return message.getId();
    }
    return existingMessage.getId();
  }

  @Override
  public void updateMessage(Message message) {
    getEntityManager().merge(message);
  }

  @Override
  public void deleteMessage(Message message) {
    EntityManager theEntityManager = getEntityManager();
    Message reattachedMessage = theEntityManager.merge(message);
    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
      for (Attachment attachment : message.getAttachments()) {
        deleteAttachmentFile(attachment);
      }
    }
    theEntityManager.remove(reattachedMessage);
  }

  @Override
  public Message findMessageById(final String id) {
    return getEntityManager().find(Message.class, id);
  }

  public Message findMessageByMailId(final String messageId, String componentId) {
    TypedQuery<Message> query = getEntityManager().createNamedQuery("findMessage", Message.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setParameter("messageId", messageId);
    Message result = null;
    try {
      result = query.getSingleResult();
    } catch (NoResultException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return result;
  }

  @Override
  public List<Message> listAllMessagesOfMailingList(final String componentId,
      final int page, final int elementsPerPage, final OrderBy orderBy) {
    TypedQuery<Message> query = getEntityManager().createQuery(
        "from Message where componentId = :componentId " + orderBy.getOrderExpression(),
        Message.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setFirstResult(page * elementsPerPage);
    query.setMaxResults(elementsPerPage);
    return query.getResultList();
  }

  @Override
  public List<Message> listDisplayableMessagesOfMailingList(String componentId,
      final int month, final int year, final int page,
      final int elementsPerPage, final OrderBy orderBy) {
    String queryText = "from Message where componentId = :componentId and moderated = :moderated";
    if (month >= 0) {
      queryText += " and month = " + month;
    }
    if (year >= 0) {
      queryText += " and year = " + year;
    }
    queryText += " " + orderBy.getOrderExpression();

    TypedQuery<Message> query = getEntityManager().createQuery(queryText, Message.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setParameter(MODERATED, true);
    query.setFirstResult(page * elementsPerPage);
    query.setMaxResults(elementsPerPage);
    return query.getResultList();
  }

  @Override
  public List<Message> listUnmoderatedMessagesOfMailingList(String componentId,
      int page, int elementsPerPage, OrderBy orderBy) {
    TypedQuery<Message> query = getEntityManager().createQuery(
        "from Message where componentId = :componentId and moderated = :moderated " + orderBy.
        getOrderExpression(), Message.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setParameter(MODERATED, false);
    query.setFirstResult(page * elementsPerPage);
    query.setMaxResults(elementsPerPage);
    return query.getResultList();
  }

  @Override
  public List<Message> listActivityMessages(String componentId, int size, OrderBy orderBy) {
    TypedQuery<Message> query = getEntityManager().createQuery(
        "from Message where componentId = :componentId and moderated = :moderated " + orderBy.
        getOrderExpression(), Message.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setParameter(MODERATED, true);
    query.setMaxResults(size);
    return query.getResultList();
  }

  @Override
  public long listTotalNumberOfMessages(String componentId) {
    TypedQuery<Long> query = getEntityManager().
        createNamedQuery("countOfMessages", Long.class);
    query.setParameter(COMPONENT_ID, componentId);
    return query.getSingleResult();
  }

  @Override
  public long listTotalNumberOfDisplayableMessages(String componentId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery(
        "countOfMessagesByModeration", Long.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setParameter(MODERATED, true);
    return query.getSingleResult();
  }

  @Override
  public long listTotalNumberOfUnmoderatedMessages(String componentId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery(
        "countOfMessagesByModeration", Long.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setParameter(MODERATED, false);
    return query.getSingleResult();
  }

  @Override
  public List<Activity> listActivity(String componentId) {
    TypedQuery<Activity> query = getEntityManager().createNamedQuery("findActivitiesFromMessages",
        Activity.class);
    query.setParameter(COMPONENT_ID, componentId);
    query.setParameter(MODERATED, true);
    return query.getResultList();
  }

  protected void saveAttachmentFile(Attachment attachment) {
    try {
      File file = new File(attachment.getPath());
      if (file.exists() && file.isFile()) {
        attachment.setSize(file.length());
        String hash = CryptMD5.encrypt(file);
        attachment.setMd5Signature(hash);
        Attachment existingFile = findAlreadyExistingAttachment(hash, file.length(),
            attachment.getFileName(), null);
        if (existingFile != null && !existingFile.getPath().equals(attachment.getPath())) {
          attachment.setPath(existingFile.getPath());
          deleteFile(file);
        }
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  protected void deleteAttachmentFile(Attachment attachment) {
    File file = new File(attachment.getPath());
    if (file.exists() && file.isFile()) {
      Attachment existingFile = findAlreadyExistingAttachment(attachment
          .getMd5Signature(), attachment.getSize(), attachment.getFileName(),
          attachment.getId());
      if (existingFile == null) {
        deleteFile(file);
      }
    }
  }

  protected Attachment findAlreadyExistingAttachment(final String md5Hash,
      final long size, final String fileName, final String attachmentId) {
    TypedQuery<Attachment> query;
    if (StringUtil.isDefined(attachmentId)) {
      query = getEntityManager().
          createNamedQuery("findSomeAttachmentsExcludingOne", Attachment.class);
      query.setParameter("id", attachmentId);
    } else {
      query = getEntityManager().createNamedQuery("findSomeAttachments", Attachment.class);
    }
    query.setParameter("md5", md5Hash);
    query.setParameter("size", size);
    query.setParameter("fileName", fileName);

    List<Attachment> attachments = query.getResultList();
    Attachment result = null;
    if (!attachments.isEmpty()) {
      result = attachments.get(0);
    }
    return result;
  }

  private void deleteFile(File file) {
    try {
      Files.delete(file.toPath());
    } catch (IOException e) {
      SilverLogger.getLogger(this).warn("Cannot delete file {0}", file.getPath());
    }
  }
}
