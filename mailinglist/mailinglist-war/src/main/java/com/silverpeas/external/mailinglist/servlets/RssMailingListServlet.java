/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.external.mailinglist.servlets;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.silverpeas.peasUtil.RssServlet;

public class RssMailingListServlet extends RssServlet<Message> {

  @Override
  public String getElementCreatorId(Message element) {
    return null;
  }

  @Override
  public String getExternalCreatorId(Message message) {
    return message.getSender();
  }

  @Override
  public Date getElementDate(Message message) {
    return message.getSentDate();
  }

  @Override
  public String getElementDescription(Message message, String userId) {
    return message.getSummary();
  }

  @Override
  public String getElementLink(Message message, String userId) {
    return ServicesFactory.getNotificationFormatter().prepareUrl(message, false);
  }

  @Override
  public String getElementTitle(Message message, String userId) {
    return message.getTitle();
  }

  @Override
  public Collection getListElements(String instanceId, int nbReturned) throws RemoteException {
    MailingList mailingList = ServicesFactory.getMailingListService().findMailingList(instanceId);
    return ServicesFactory.getMessageService().listDisplayableMessages(mailingList, nbReturned,
        new OrderBy("sentDate", true));
  }
}
