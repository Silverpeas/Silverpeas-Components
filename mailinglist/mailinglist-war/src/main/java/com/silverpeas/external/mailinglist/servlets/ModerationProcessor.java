/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.external.mailinglist.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;

public class ModerationProcessor implements MailingListRoutage {

  public static String processModeration(RestRequest rest,
      HttpServletRequest request) {
    switch (rest.getAction()) {
    case RestRequest.DELETE:
    case RestRequest.UPDATE:
    case RestRequest.FIND:
    default:
      int page = 0;
      if (request.getParameter(CURRENT_PAGE_PARAM) != null) {
        try {
          page = Integer.parseInt(request.getParameter(CURRENT_PAGE_PARAM));
        } catch (NumberFormatException nfex) {

        }
      }
      String id = (String) rest.getElements().get(DESTINATION_MODERATION);
      MailingList list = ServicesFactory.getMailingListService().findMailingList(id);
      String orderParam = request.getParameter(ORDER_BY_PARAM);
      String ascendantParam = request.getParameter(ORDER_ASC_PARAM);
      boolean asc = false;
      if (orderParam == null) {
        orderParam = "sentDate";
      }
      if (ascendantParam != null) {
        asc = Boolean.valueOf(ascendantParam).booleanValue();
      }
      OrderBy orderBy = new OrderBy(orderParam, asc);
      request.setAttribute(orderParam, new Boolean(!asc));
      List<Message> messages = ServicesFactory.getMessageService()
          .listUnmoderatedeMessages(list, page, orderBy);
      request.setAttribute(MESSAGES_LIST_ATT, messages);
      int nbPages = ServicesFactory.getMessageService()
          .getNumberOfPagesForUnmoderatedMessages(list);
      request.setAttribute(NB_PAGE_ATT, new Integer(nbPages));
      return JSP_BASE + DESTINATION_DISPLAY_MODERATION;
    }
  }
}
