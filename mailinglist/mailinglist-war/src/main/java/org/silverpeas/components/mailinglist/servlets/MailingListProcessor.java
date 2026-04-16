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
package org.silverpeas.components.mailinglist.servlets;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.MessageService;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.util.OrderBy;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.List;

import static org.silverpeas.components.mailinglist.servlets.MailingListRoutageProperties.*;

@Bean
public class MailingListProcessor {

  @Inject
  private MailingListService mailingListService;

  @Inject
  private MessageService messageService;

  public String processMailingList(RestRequest rest, HttpServletRequest request) {
    switch (rest.getAction()) {
      case RestRequest.DELETE:
      case RestRequest.UPDATE:
      case RestRequest.FIND:
      default:
        int page = getPage(request);
        int year = getYear(rest);
        int month = getMonth(rest);
        String id = rest.getElements().get(DESTINATION_LIST);
        MailingList list = mailingListService.findMailingList(id);
        String orderParam = request.getParameter(ORDER_BY_PARAM);
        String ascendantParam = request.getParameter(ORDER_ASC_PARAM);
        boolean asc = false;
        if (orderParam == null) {
          orderParam = "sentDate";
        }
        if (ascendantParam != null) {
          asc = Boolean.parseBoolean(ascendantParam);
        }
        request.setAttribute(orderParam, !asc);
        OrderBy orderBy = new OrderBy(orderParam, asc);
        List<Message> messages =
            messageService.listDisplayableMessages(list, month, year, page, orderBy);
        request.setAttribute(MESSAGES_LIST_ATT, messages);
        int nbPages = messageService.getNumberOfPagesForDisplayableMessages(list);
        request.setAttribute(NB_PAGE_ATT, nbPages);
        request.setAttribute(CURRENT_PAGE_ATT, page);
        return JSP_BASE + DESTINATION_DISPLAY_LIST;
    }
  }

  private int getMonth(RestRequest rest) {
    return decodeParam(rest.getElements().get(CURRENT_MONTH_PARAM), -1);
  }

  private int getYear(RestRequest rest) {
    return decodeParam(rest.getElements().get(CURRENT_YEAR_PARAM), -1);
  }

  private int decodeParam(String param, int defaultValue) {
    int year = defaultValue;
    if (param != null) {
      try {
        year = Integer.parseInt(param);
      } catch (NumberFormatException e) {
        SilverLogger.getLogger(this).error(e.getMessage());
      }
    }
    return year;
  }

  private int getPage(HttpServletRequest request) {
    return decodeParam(request.getParameter(CURRENT_PAGE_PARAM), 0);
  }
}
