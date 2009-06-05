package com.silverpeas.mailinglist.service.model;

import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.MailingListActivity;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;

public interface MessageService {

  public String saveMessage(Message message);

  public void moderateMessage(String id);

  public void deleteMessage(String id);

  public Message getMessage(String id);

  public List<Message> listMessages(MailingList mailingList, int pageNumber,
      OrderBy orderBy);

  public List<Message> listDisplayableMessages(MailingList mailingList,
      int month, int year, int pageNumber, OrderBy orderBy);

  public List<Message> listDisplayableMessages(MailingList mailingList,
      int number, OrderBy orderBy);

  public List<Message> listUnmoderatedeMessages(MailingList mailingList,
      int pageNumber, OrderBy orderBy);

  public int getNumberOfPagesForUnmoderatedMessages(MailingList mailingList);

  public int getNumberOfPagesForDisplayableMessages(MailingList mailingList);

  public int getNumberOfPagesForAllMessages(MailingList mailingList);
  
  public int getTotalNumberOfMessages(MailingList mailingList);

  public void setElementsPerPage(int elementsPerPage);

  public MailingListActivity getActivity(MailingList mailingList);

}