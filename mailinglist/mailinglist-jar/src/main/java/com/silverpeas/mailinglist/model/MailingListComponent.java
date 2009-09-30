package com.silverpeas.mailinglist.model;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.notification.NotificationHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MailingListComponent implements MessageListener {
  private String componentId;

  public MailingListComponent(String componentId) {
    this.componentId = componentId;
  }

  public boolean checkSender(String email) {
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        componentId);
    return list.isOpen() || list.isEmailAuthorized(email);
  }

  public String getComponentId() {
    return componentId;
  }

  public void onMessage(MessageEvent event) {
    if (event == null || event.getMessages() == null
        || event.getMessages().isEmpty()) {
      return;
    }
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        componentId);
    for (Message message : event.getMessages()) {
      message.setModerated(!list.isModerated());
      ServicesFactory.getMessageService().saveMessage(message);
    }
    if (list.isNotify() || list.isModerated()) {
      NotificationHelper helper = ServicesFactory.getNotificationHelper();
      for (Message message : event.getMessages()) {
        try {
          helper.notify(message, list);
        } catch (Exception e) {
          SilverTrace.error("mailingList", "MailingListComponent.onMessage",
              "mailinglist.notification.error", e);
          e.printStackTrace();
        }
      }
    }
  }
}
