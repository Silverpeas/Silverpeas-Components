/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.mailinglist;

import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.MessageIndexer;
import com.silverpeas.mailinglist.service.model.MessageService;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexation;
import com.stratelia.webactiv.beans.admin.ComponentInst;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("MailinglistComponentIndexation")
public class MailinglistIndexer implements ComponentIndexation {

  @Inject
  private MailingListService mailingListService;
  @Inject
  private MessageService messageService;

  @Override
  public void index(ComponentInst componentInst) throws Exception {
    List<MailingList> mailingLists = mailingListService.listAllMailingLists();
    for (MailingList mailingList : mailingLists) {
      List<Message> messages = messageService.listDisplayableMessages(mailingList, -1, -1, 0,
          new OrderBy("sentDate", true));
      for (Message message : messages) {
        MessageIndexer.indexMessage(message);
      }
    }
  }
}
