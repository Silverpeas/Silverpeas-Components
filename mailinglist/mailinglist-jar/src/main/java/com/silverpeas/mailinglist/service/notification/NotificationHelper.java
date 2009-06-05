package com.silverpeas.mailinglist.service.notification;

import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;

public interface NotificationHelper {  
    
  public void notify(Message message, MailingList list) throws Exception;

}