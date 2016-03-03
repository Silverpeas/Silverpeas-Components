/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.model;

import org.silverpeas.components.mailinglist.service.MailingListServicesProvider;
import org.silverpeas.components.mailinglist.service.event.MessageEvent;
import org.silverpeas.components.mailinglist.service.event.MessageListener;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.notification.NotificationHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MailingListComponent implements MessageListener {

  private String componentId;

  public MailingListComponent(String componentId) {
    this.componentId = componentId;
  }

  @Override
  public boolean checkSender(String email) {
    MailingList list =
        MailingListServicesProvider.getMailingListService().findMailingList(componentId);
    return list.isOpen() || list.isEmailAuthorized(email);
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  @Override
  public void onMessage(MessageEvent event) {
    if (event == null || event.getMessages() == null || event.getMessages().isEmpty()) {
      return;
    }
    MailingList list =
        MailingListServicesProvider.getMailingListService().findMailingList(componentId);
    for (Message message : event.getMessages()) {
      message.setModerated(!list.isModerated());
      MailingListServicesProvider.getMessageService().saveMessage(message);
    }
    if (list.isNotify() || list.isModerated()) {
      NotificationHelper helper = MailingListServicesProvider.getNotificationHelper();
      for (Message message : event.getMessages()) {
        try {
          helper.notify(message, list);
        } catch (Exception e) {
          SilverTrace.error("mailinglist", "MailingListComponent.onMessage",
              "mailinglist.notification.error", e);
        }
      }
    }
  }
}
