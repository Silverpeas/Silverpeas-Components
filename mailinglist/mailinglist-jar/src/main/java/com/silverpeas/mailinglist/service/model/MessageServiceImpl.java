package com.silverpeas.mailinglist.service.model;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.Activity;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.MailingListActivity;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.model.dao.MessageDao;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarException;
import com.stratelia.webactiv.calendar.model.ToDoHeader;

public class MessageServiceImpl implements MessageService {
  private MessageDao messageDao;

  private int elementsPerPage = 10;

  private CalendarBm calendarBm;

  private static final int MSG_PER_ACTIVITY = 5;

  public int getElementsPerPage() {
    return elementsPerPage;
  }

  public void setElementsPerPage(final int elementsPerPage) {
    this.elementsPerPage = elementsPerPage;
  }

  public MessageDao getMessageDao() {
    return messageDao;
  }

  public void setMessageDao(final MessageDao messageDao) {
    this.messageDao = messageDao;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.mailinglist.service.model.MessageService#saveMessage(com
   * .silverpeas.mailinglist.service.model.beans.Message)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.mailinglist.service.model.MessageService#getMessage(java
   * .lang.String)
   */
  public Message getMessage(final String id) {
    if (id == null) {
      return null;
    }
    return messageDao.findMessageById(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.mailinglist.service.model.MessageService#listMessages(com
   * .silverpeas.mailinglist.service.model.beans.MailingList, int, int)
   */
  public List<Message> listMessages(final MailingList mailingList,
      final int pageNumber, final OrderBy orderBy) {
    return messageDao
        .listAllMessagesOfMailingList(mailingList.getComponentId(), pageNumber,
            this.elementsPerPage, orderBy);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.mailinglist.service.model.MessageService#listDisplayableMessages
   * (com.silverpeas.mailinglist.service.model.beans.MailingList, int, int)
   */
  public List<Message> listDisplayableMessages(final MailingList mailingList,
      final int month, final int year, final int pageNumber,
      final OrderBy orderBy) {
    return messageDao.listDisplayableMessagesOfMailingList(mailingList
        .getComponentId(), month, year, pageNumber, this.elementsPerPage,
        orderBy);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.silverpeas.mailinglist.service.model.MessageService#
   * listUnmoderatedeMessages
   * (com.silverpeas.mailinglist.service.model.beans.MailingList, int, int)
   */
  public List<Message> listUnmoderatedeMessages(final MailingList mailingList,
      final int pageNumber, final OrderBy orderBy) {
    return messageDao.listUnmoderatedMessagesOfMailingList(mailingList
        .getComponentId(), pageNumber, this.elementsPerPage, orderBy);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.silverpeas.mailinglist.service.model.MessageService#
   * getNumberOfPagesForUnmoderatedMessages
   * (com.silverpeas.mailinglist.service.model.beans.MailingList, int)
   */
  public int getNumberOfPagesForUnmoderatedMessages(
      final MailingList mailingList) {
    final int nbElements = messageDao
        .listTotalNumberOfUnmoderatedMessages(mailingList.getComponentId());
    return getNumberOfPages(nbElements);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.silverpeas.mailinglist.service.model.MessageService#
   * getNumberOfPagesForDisplayableMessages
   * (com.silverpeas.mailinglist.service.model.beans.MailingList, int)
   */
  public int getNumberOfPagesForDisplayableMessages(
      final MailingList mailingList) {
    final int nbElements = messageDao
        .listTotalNumberOfDisplayableMessages(mailingList.getComponentId());
    return getNumberOfPages(nbElements);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.silverpeas.mailinglist.service.model.MessageService#
   * getNumberOfPagesForAllMessages
   * (com.silverpeas.mailinglist.service.model.beans.MailingList, int)
   */
  public int getNumberOfPagesForAllMessages(final MailingList mailingList) {
    final int nbElements = messageDao.listTotalNumberOfMessages(mailingList
        .getComponentId());
    return getNumberOfPages(nbElements);
  }

  private int getNumberOfPages(final int nbElements) {
    int nbPages = nbElements / this.elementsPerPage;
    if (nbElements % this.elementsPerPage != 0) {
      nbPages = nbPages + 1;
    }
    return nbPages;
  }

  public MailingListActivity getActivity(final MailingList mailingList) {
    if (mailingList == null) {
      return null;
    }
    List<Message> messages = messageDao.listActivityMessages(mailingList
        .getComponentId(), MSG_PER_ACTIVITY, new OrderBy("sentDate", false));
    List<Activity> activities = messageDao.listActivity(mailingList
        .getComponentId());
    Collections.sort(activities);
    return new MailingListActivity(messages, activities);
  }

  @SuppressWarnings("unchecked")
  public void deleteMessage(String id) {
    Message message = messageDao.findMessageById(id);
    if (message != null) {
      messageDao.deleteMessage(message);
      MessageIndexer.unindexMessage(message);
      try {
        Collection<ToDoHeader> todos = calendarBm.getOrganizerToDos(message
            .getComponentId());
        if (todos != null && !todos.isEmpty()) {
          for (ToDoHeader todo : todos) {
            if (id.equalsIgnoreCase(todo.getDescription())) {
              todo.setCompletedDate(new Date());
              todo.setPercentCompleted(100);
              getCalendarBm().updateToDo(todo);
              return;
            }
          }
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (CalendarException e) {
        e.printStackTrace();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void moderateMessage(String id) {
    Message message = messageDao.findMessageById(id);
    if (message != null) {
      message.setModerated(true);
      messageDao.updateMessage(message);
      MessageIndexer.indexMessage(message);
    }
    try {
      Collection<ToDoHeader> todos = calendarBm.getOrganizerToDos(message
          .getComponentId());
      if (todos != null && !todos.isEmpty()) {
        for (ToDoHeader todo : todos) {
          if (id.equalsIgnoreCase(todo.getDescription())) {
            todo.setCompletedDate(new Date());
            todo.setPercentCompleted(100);
            getCalendarBm().updateToDo(todo);
            return;
          }
        }
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (CalendarException e) {
      e.printStackTrace();
    }
  }

  public List<Message> listDisplayableMessages(MailingList mailingList,
      int number, OrderBy orderBy) {
    return messageDao.listDisplayableMessagesOfMailingList(mailingList
        .getComponentId(), -1, -1, 0, number, orderBy);
  }

  public CalendarBm getCalendarBm() {
    return calendarBm;
  }

  public void setCalendarBm(CalendarBm calendarBm) {
    this.calendarBm = calendarBm;
  }

  @Override
  public int getTotalNumberOfMessages(MailingList mailingList) {
    return messageDao.listTotalNumberOfMessages(mailingList.getComponentId());
  }


}
