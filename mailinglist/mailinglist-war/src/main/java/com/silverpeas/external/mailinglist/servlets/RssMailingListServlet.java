package com.silverpeas.external.mailinglist.servlets;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.silverpeas.peasUtil.RssServlet;

public class RssMailingListServlet extends RssServlet {

  public String getElementCreatorId(Object element) {
    return null;
  }

  public String getExternalCreatorId(Object element) {
    Message message = (Message) element;
    return message.getSender();
  }

  public Date getElementDate(Object element) {
    Message message = (Message) element;
    return message.getSentDate();
  }

  public String getElementDescription(Object element, String userId) {
    Message message = (Message) element;
    return message.getSummary();
  }

  public String getElementLink(Object element, String userId) {
    Message message = (Message) element;
    return ServicesFactory
        .getNotificationFormatter().prepareUrl("", message, false);
  }

  public String getElementTitle(Object element, String userId) {
    Message message = (Message) element;
    return message.getTitle();
  }

  public Collection getListElements(String instanceId, int nbReturned)
      throws RemoteException {
    MailingList mailingList = ServicesFactory.getMailingListService()
        .findMailingList(instanceId);
    return ServicesFactory.getMessageService().listDisplayableMessages(
        mailingList, nbReturned, new OrderBy("sentDate", true));
  }
}
