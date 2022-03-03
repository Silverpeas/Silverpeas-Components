/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.mailinglist.servlets;

import org.silverpeas.core.web.util.servlet.RssServlet;
import org.silverpeas.components.mailinglist.service.MailingListServicesProvider;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.MessageService;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.util.OrderBy;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;

public class RssMailingListServlet extends RssServlet<Message> {

  @Inject
  private MailingListService mailingListService;
  @Inject
  private MessageService messageService;

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
    return MailingListServicesProvider.getNotificationFormatter().prepareUrl(message, false);
  }

  @Override
  public String getElementTitle(Message message, String userId) {
    return message.getTitle();
  }

  @Override
  public Collection getListElements(String instanceId, int nbReturned) {
    MailingList mailingList = mailingListService.findMailingList(instanceId);
    return messageService
        .listDisplayableMessages(mailingList, nbReturned, new OrderBy("sentDate", true));
  }
}
