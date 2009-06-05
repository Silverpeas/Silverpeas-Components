package com.silverpeas.mailinglist.service.model.dao;

import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.Activity;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;

public interface MessageDao {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.mailinglist.service.model.dao.MessageDao#saveMessage(com
   * .silverpeas.mailinglist.service.model.Message)
   */
  public String saveMessage(Message message);

  public void updateMessage(Message message);

  public void deleteMessage(Message message);

  public Message findMessageById(String id);

  public List<Message> listAllMessagesOfMailingList(String componentId,
      int page, int elementsPerPage, OrderBy orderBy);

  public List<Message> listDisplayableMessagesOfMailingList(String componentId,
      int month, int year, int page, int elementsPerPage, OrderBy orderBy);

  public List<Message> listUnmoderatedMessagesOfMailingList(String componentId,
      int page, int elementsPerPage, OrderBy orderBy);

  public List<Activity> listActivity(String componentId);

  public List<Message> listActivityMessages(String componentId, int size,
      OrderBy orderBy);

  public int listTotalNumberOfMessages(String componentId);

  public int listTotalNumberOfDisplayableMessages(String componentId);

  public int listTotalNumberOfUnmoderatedMessages(String componentId);
}