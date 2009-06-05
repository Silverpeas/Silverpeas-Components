package com.silverpeas.mailinglist;

import java.util.List;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.MessageIndexer;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;

public class MailinglistIndexer implements ComponentIndexerInterface {

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {
    List<MailingList> mailingLists = ServicesFactory.getMailingListService()
        .listAllMailingLists();
    for (MailingList mailingList : mailingLists) {
      List<Message> messages = ServicesFactory.getMessageService()
          .listDisplayableMessages(mailingList, -1, -1, 0,
              new OrderBy("sentDate", true));
      for (Message message : messages) {
        MessageIndexer.indexMessage(message);
      }
    }
  }
}