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
package com.silverpeas.external.mailinglist.servlets;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

public class UsersProcessor implements MailingListRoutage {

  public static final int ELEMENTS_PER_PAGE = 10;

  public static String processUsers(RestRequest rest, HttpServletRequest request) {
    String id = (String) rest.getElements().get(DESTINATION_USERS);
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    switch (rest.getAction()) {
      case RestRequest.DELETE:
        if (((Boolean) request.getAttribute(IS_USER_ADMIN_ATT)).booleanValue()) {
          String[] emails = request.getParameterValues(SELECTED_USERS_PARAM);
          if (emails != null && emails.length > 0) {
            Set<ExternalUser> users = new HashSet<ExternalUser>(emails.length);
            for (int i = 0; i < emails.length; i++) {
              ExternalUser user = new ExternalUser();
              user.setComponentId(rest.getComponentId());
              user.setEmail(emails[i]);
              users.add(user);
            }
            servicesFactory.getMailingListService().removeExternalUsers(
                rest.getComponentId(), users);
          } else {
            if (id != null && !DELETE_ACTION.equalsIgnoreCase(id)
                && !UPDATE_ACTION.equalsIgnoreCase(id)) {
              ExternalUser user = new ExternalUser();
              user.setComponentId(rest.getComponentId());
              user.setEmail(id);
              servicesFactory.getMailingListService().removeExternalUser(
                  rest.getComponentId(), user);
            }
          }
        }
        break;
      case RestRequest.CREATE:
        if (((Boolean) request.getAttribute(IS_USER_ADMIN_ATT)).booleanValue()) {
          String emails = request.getParameter(USERS_LIST_PARAM);
          if (emails != null) {
            StringTokenizer tokenizer = new StringTokenizer(emails, ";", false);
            Set<ExternalUser> users = new HashSet<ExternalUser>(100);
            while (tokenizer.hasMoreTokens()) {
              String email = tokenizer.nextToken();
              boolean isValid = false;
              try {
                isValid = (new InternetAddress(email) != null);
              } catch (AddressException ex) {
              }
              if (email != null && isValid) {
                ExternalUser user = new ExternalUser();
                user.setComponentId(rest.getComponentId());
                user.setEmail(email);
                users.add(user);
              }
            }
            servicesFactory.getMailingListService().addExternalUsers(
                rest.getComponentId(), users);
          }
        }
        break;
      case RestRequest.UPDATE:
      case RestRequest.FIND:
      default:
    }
    return prepareUsersList(request, rest);
  }

  @SuppressWarnings("unchecked")
  protected static String prepareUsersList(HttpServletRequest request,
      RestRequest rest) {
    if (((Boolean) request.getAttribute(IS_USER_ADMIN_ATT)).booleanValue()) {
      MailingList list = ServicesFactory.getFactory().getMailingListService()
          .findMailingList(rest.getComponentId());
      int page = 0;
      if (request.getParameter(CURRENT_PAGE_PARAM) != null) {
        try {
          page = Integer.parseInt(request.getParameter(CURRENT_PAGE_PARAM));
        } catch (NumberFormatException nfex) {
        }
      }
      List<ExternalUser> users = new ArrayList<ExternalUser>(list.getExternalSubscribers());
      Collections.sort(users);
      int fromIndex = page * ELEMENTS_PER_PAGE;
      int endIndex = fromIndex + ELEMENTS_PER_PAGE;
      if (endIndex >= users.size()) {
        endIndex = users.size();
      }
      if (!users.isEmpty() && endIndex > fromIndex) {
        request
            .setAttribute(USERS_LIST_ATT, users.subList(fromIndex, endIndex));
        request.setAttribute(CURRENT_PAGE_PARAM, new Integer(page));
        request.setAttribute(NB_PAGE_ATT, new Integer(getNumberOfPages(users
            .size())));
        return JSP_BASE + DESTINATION_DISPLAY_USERS;
      }
    }
    request.setAttribute(USERS_LIST_ATT, new ArrayList());
    request.setAttribute(CURRENT_PAGE_PARAM, new Integer(0));
    request.setAttribute(NB_PAGE_ATT, new Integer(0));
    return JSP_BASE + DESTINATION_DISPLAY_USERS;
  }

  private static int getNumberOfPages(final int nbElements) {
    int nbPages = nbElements / ELEMENTS_PER_PAGE;
    if (nbElements % ELEMENTS_PER_PAGE != 0) {
      nbPages = nbPages + 1;
    }
    return nbPages;
  }
}
