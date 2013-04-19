/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
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

  @Override
  public boolean checkSender(String email) {
    MailingList list = ServicesFactory.getFactory().getMailingListService().findMailingList(
        componentId);
    return list.isOpen() || list.isEmailAuthorized(email);
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  @Override
  public void onMessage(MessageEvent event) {
    if (event == null || event.getMessages() == null
        || event.getMessages().isEmpty()) {
      return;
    }
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MailingList list = servicesFactory.getMailingListService().findMailingList(
        componentId);
    for (Message message : event.getMessages()) {
      message.setModerated(!list.isModerated());
      servicesFactory.getMessageService().saveMessage(message);
    }
    if (list.isNotify() || list.isModerated()) {
      NotificationHelper helper = servicesFactory.getNotificationHelper();
      for (Message message : event.getMessages()) {
        try {
          helper.notify(message, list);
        } catch (Exception e) {
          SilverTrace.error("mailingList", "MailingListComponent.onMessage",
              "mailinglist.notification.error", e);
        }
      }
    }
  }
}
