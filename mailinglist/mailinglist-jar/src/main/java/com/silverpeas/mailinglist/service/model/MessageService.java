/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mailinglist.service.model;

import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.MailingListActivity;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;

public interface MessageService {

  public String saveMessage(Message message);

  public void moderateMessage(String id);

  public void deleteMessage(String id);

  public Message getMessage(String id);

  public List<Message> listMessages(MailingList mailingList, int pageNumber,
      OrderBy orderBy);

  public List<Message> listDisplayableMessages(MailingList mailingList,
      int month, int year, int pageNumber, OrderBy orderBy);

  public List<Message> listDisplayableMessages(MailingList mailingList,
      int number, OrderBy orderBy);

  public List<Message> listUnmoderatedeMessages(MailingList mailingList,
      int pageNumber, OrderBy orderBy);

  public int getNumberOfPagesForUnmoderatedMessages(MailingList mailingList);

  public int getNumberOfPagesForDisplayableMessages(MailingList mailingList);

  public int getNumberOfPagesForAllMessages(MailingList mailingList);

  public int getTotalNumberOfMessages(MailingList mailingList);

  public void setElementsPerPage(int elementsPerPage);

  public MailingListActivity getActivity(MailingList mailingList);

}