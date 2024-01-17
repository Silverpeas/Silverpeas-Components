/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.model.dao;

import org.silverpeas.components.mailinglist.service.model.beans.Activity;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.util.OrderBy;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

public interface MessageDao {

  static MessageDao get() {
    return ServiceProvider.getService(MessageDao.class);
  }

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

  public long listTotalNumberOfMessages(String componentId);

  public long listTotalNumberOfDisplayableMessages(String componentId);

  public long listTotalNumberOfUnmoderatedMessages(String componentId);
}
