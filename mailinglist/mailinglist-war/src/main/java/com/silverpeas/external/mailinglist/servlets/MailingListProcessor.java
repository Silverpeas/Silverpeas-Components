package com.silverpeas.external.mailinglist.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.OrderBy;

public class MailingListProcessor implements MailingListRoutage {

  public static String processMailingList(RestRequest rest,
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
      int year = -1;
      if (rest.getElements().get(CURRENT_YEAR_PARAM) != null) {
        try {
          year = Integer.parseInt((String)rest.getElements().get(CURRENT_YEAR_PARAM));
        } catch (NumberFormatException nfex) {
          year = -1;
        }
      }
      int month = -1;
      if (rest.getElements().get(CURRENT_MONTH_PARAM) != null) {
        try {
          month = Integer.parseInt((String)rest.getElements().get(CURRENT_MONTH_PARAM));
        } catch (NumberFormatException nfex) {
          month = -1;
        }
      }
      String id = (String) rest.getElements().get(DESTINATION_LIST);
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
      request.setAttribute(orderParam, new Boolean(!asc));
      OrderBy orderBy = new OrderBy(orderParam, asc);
      List<Message> messages = ServicesFactory.getMessageService()
          .listDisplayableMessages(list, month, year, page, orderBy);
      request.setAttribute(MESSAGES_LIST_ATT, messages);
      int nbPages = ServicesFactory.getMessageService()
          .getNumberOfPagesForDisplayableMessages(list);
      request.setAttribute(NB_PAGE_ATT, new Integer(nbPages));
      request.setAttribute(CURRENT_PAGE_ATT, new Integer(page));
      return JSP_BASE + DESTINATION_DISPLAY_LIST;
    }
  }
}
