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
package org.silverpeas.components.mailinglist.service.model;

import org.silverpeas.components.mailinglist.service.model.beans.Activity;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.MailingListActivity;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.model.dao.MessageDao;
import org.silverpeas.components.mailinglist.service.util.OrderBy;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.personalorganizer.model.ToDoHeader;
import org.silverpeas.core.personalorganizer.service.CalendarRuntimeException;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

  @Inject
  private MessageDao messageDao;
  private int elementsPerPage = 10;
  @Inject
  private SilverpeasCalendar calendar;
  private static final int MSG_PER_ACTIVITY = 5;

  public int getElementsPerPage() {
    return elementsPerPage;
  }

  @Override
  public void setElementsPerPage(final int elementsPerPage) {
    this.elementsPerPage = elementsPerPage;
  }

  public MessageDao getMessageDao() {
    return messageDao;
  }

  @Override
  public String saveMessage(final Message message) {
    if (message == null) {
      return null;
    }
    String id = message.getId();
    if (id == null) {
      id = messageDao.saveMessage(message);
      MessageIndexer.indexMessage(message);
    } else {
      messageDao.updateMessage(message);
    }
    return id;
  }

  @Override
  public Message getMessage(final String id) {
    return messageDao.findMessageById(id);
  }

  @Override
  public List<Message> listMessages(final MailingList mailingList, final int pageNumber,
      final OrderBy orderBy) {
    return messageDao.listAllMessagesOfMailingList(mailingList.getComponentId(), pageNumber,
        this.elementsPerPage, orderBy);
  }

  @Override
  public List<Message> listDisplayableMessages(final MailingList mailingList, final int month,
      final int year, final int pageNumber, final OrderBy orderBy) {
    return messageDao
        .listDisplayableMessagesOfMailingList(mailingList.getComponentId(), month, year, pageNumber,
            this.elementsPerPage, orderBy);
  }

  @Override
  public List<Message> listUnmoderatedeMessages(final MailingList mailingList, final int pageNumber,
      final OrderBy orderBy) {
    return messageDao.listUnmoderatedMessagesOfMailingList(mailingList.getComponentId(), pageNumber,
        this.elementsPerPage, orderBy);
  }

  @Override
  public int getNumberOfPagesForUnmoderatedMessages(final MailingList mailingList) {
    final long nbElements =
        messageDao.listTotalNumberOfUnmoderatedMessages(mailingList.getComponentId());
    return getNumberOfPages(nbElements);
  }

  @Override
  public int getNumberOfPagesForDisplayableMessages(final MailingList mailingList) {
    final long nbElements =
        messageDao.listTotalNumberOfDisplayableMessages(mailingList.getComponentId());
    return getNumberOfPages(nbElements);
  }

  @Override
  public int getNumberOfPagesForAllMessages(final MailingList mailingList) {
    final long nbElements = messageDao.listTotalNumberOfMessages(mailingList.getComponentId());
    return getNumberOfPages(nbElements);
  }

  private int getNumberOfPages(final long nbElements) {
    int nbPages = (int) (nbElements / this.elementsPerPage);
    if (nbElements % this.elementsPerPage != 0) {
      nbPages = nbPages + 1;
    }
    return nbPages;
  }

  @Override
  public MailingListActivity getActivity(final MailingList mailingList) {
    if (mailingList == null) {
      return null;
    }
    List<Message> messages = messageDao
        .listActivityMessages(mailingList.getComponentId(), MSG_PER_ACTIVITY,
            new OrderBy("sentDate", false));
    List<Activity> activities = messageDao.listActivity(mailingList.getComponentId());
    Collections.sort(activities);
    return new MailingListActivity(messages, activities);
  }

  @Override
  public void deleteMessage(String id) {
    Message message = messageDao.findMessageById(id);
    if (message != null) {
      messageDao.deleteMessage(message);
      MessageIndexer.unindexMessage(message);
      try {
        Collection<ToDoHeader> todos = getCalendar().getOrganizerToDos(message.getComponentId());
        if (todos != null && !todos.isEmpty()) {
          for (ToDoHeader todo : todos) {
            if (id.equalsIgnoreCase(todo.getDescription())) {
              todo.setCompletedDate(new Date());
              todo.setPercentCompleted(100);
              getCalendar().updateToDo(todo);
              return;
            }
          }
        }
      } catch (CalendarRuntimeException ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      }
    }
  }

  @Override
  public void moderateMessage(String id) {
    Message message = messageDao.findMessageById(id);
    if (message != null) {
      message.setModerated(true);
      messageDao.updateMessage(message);
      MessageIndexer.indexMessage(message);
    }
    try {
      if (message != null) {
        Collection<ToDoHeader> todos = getCalendar().getOrganizerToDos(message.getComponentId());
        if (todos != null && !todos.isEmpty()) {
          for (ToDoHeader todo : todos) {
            if (id.equalsIgnoreCase(todo.getDescription())) {
              todo.setCompletedDate(new Date());
              todo.setPercentCompleted(100);
              getCalendar().updateToDo(todo);
              return;
            }
          }
        }
      }
    } catch (CalendarRuntimeException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  @Override
  public List<Message> listDisplayableMessages(MailingList mailingList, int number,
      OrderBy orderBy) {
    return messageDao
        .listDisplayableMessagesOfMailingList(mailingList.getComponentId(), -1, -1, 0, number,
            orderBy);
  }

  public SilverpeasCalendar getCalendar() {
    return calendar;
  }

  @Override
  public long getTotalNumberOfMessages(MailingList mailingList) {
    return messageDao.listTotalNumberOfMessages(mailingList.getComponentId());
  }
}
