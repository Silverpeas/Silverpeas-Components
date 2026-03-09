/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.model;

import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.MailingListActivity;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.util.OrderBy;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

public interface MessageService {

  static MessageService get() {
    return ServiceProvider.getService(MessageService.class);
  }

  String saveMessage(Message message);

  void moderateMessage(String id);

  void deleteMessage(String id);

  Message getMessage(String id);

  List<Message> listMessages(MailingList mailingList, int pageNumber,
      OrderBy orderBy);

  List<Message> listDisplayableMessages(MailingList mailingList,
      int month, int year, int pageNumber, OrderBy orderBy);

  List<Message> listDisplayableMessages(MailingList mailingList,
      int number, OrderBy orderBy);

  List<Message> listUnmoderatedeMessages(MailingList mailingList,
      int pageNumber, OrderBy orderBy);

  int getNumberOfPagesForUnmoderatedMessages(MailingList mailingList);

  int getNumberOfPagesForDisplayableMessages(MailingList mailingList);

  int getNumberOfPagesForAllMessages(MailingList mailingList);

  long getTotalNumberOfMessages(MailingList mailingList);

  void setElementsPerPage(int elementsPerPage);

  MailingListActivity getActivity(MailingList mailingList);
}
