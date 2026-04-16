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
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.beans.ExternalUser;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.*;

import static org.silverpeas.components.mailinglist.servlets.MailingListRoutageProperties.*;

@Bean
public class UsersProcessor {

  public static final int ELEMENTS_PER_PAGE = 10;

  @Inject
  private MailingListService service;

  public String processUsers(RestRequest rest, HttpServletRequest request) {
    String id = rest.getElements().get(DESTINATION_USERS);
    boolean isAdminUser = (boolean) request.getAttribute(IS_USER_ADMIN_ATT);
    switch (rest.getAction()) {
      case RestRequest.DELETE:
        if (isAdminUser) {
          removeExternalUsers(rest, request, id);
        }
        break;
      case RestRequest.CREATE:
        if (isAdminUser) {
          String emails = request.getParameter(USERS_LIST_PARAM);
          if (emails != null) {
            Set<ExternalUser> users = getExternalUsers(rest, emails);
            service.addExternalUsers(rest.getComponentId(), users);
          }
        }
        break;
      case RestRequest.UPDATE:
      case RestRequest.FIND:
      default:
    }
    return prepareUsersList(request, rest);
  }

  private void removeExternalUsers(RestRequest rest, HttpServletRequest request, String id) {
    String[] emails = request.getParameterValues(SELECTED_USERS_PARAM);
    if (emails != null && emails.length > 0) {
      Set<ExternalUser> users = new HashSet<>(emails.length);
      for (final String email : emails) {
        ExternalUser user = new ExternalUser();
        user.setComponentId(rest.getComponentId());
        user.setEmail(email);
        users.add(user);
      }
      service.removeExternalUsers(rest.getComponentId(), users);
    } else {
      if (id != null && !DELETE_ACTION.equalsIgnoreCase(id) &&
          !UPDATE_ACTION.equalsIgnoreCase(id)) {
        ExternalUser user = new ExternalUser();
        user.setComponentId(rest.getComponentId());
        user.setEmail(id);
        service.removeExternalUser(rest.getComponentId(), user);
      }
    }
  }

  private static @NonNull Set<ExternalUser> getExternalUsers(RestRequest rest, String emails) {
    StringTokenizer tokenizer = new StringTokenizer(emails, ";", false);
    Set<ExternalUser> users = new HashSet<>(100);
    while (tokenizer.hasMoreTokens()) {
      String email = tokenizer.nextToken();
      boolean isValid;
      try {
        new InternetAddress(email);
        isValid = true;
      } catch (AddressException ex) {
        isValid = false;
      }
      if (isValid) {
        ExternalUser user = new ExternalUser();
        user.setComponentId(rest.getComponentId());
        user.setEmail(email);
        users.add(user);
      }
    }
    return users;
  }

  protected String prepareUsersList(HttpServletRequest request, RestRequest rest) {
    boolean isAdminUser = (boolean) request.getAttribute(IS_USER_ADMIN_ATT);
    if (isAdminUser) {
      MailingList list = service.findMailingList(rest.getComponentId());
      int page = 0;
      if (request.getParameter(CURRENT_PAGE_PARAM) != null) {
        try {
          page = Integer.parseInt(request.getParameter(CURRENT_PAGE_PARAM));
        } catch (NumberFormatException e) {
          SilverLogger.getLogger(this).error(e.getMessage());
        }
      }
      List<ExternalUser> users = new ArrayList<>(list.getExternalSubscribers());
      Collections.sort(users);
      int fromIndex = page * ELEMENTS_PER_PAGE;
      int endIndex = fromIndex + ELEMENTS_PER_PAGE;
      if (endIndex >= users.size()) {
        endIndex = users.size();
      }
      if (!users.isEmpty() && endIndex > fromIndex) {
        request.setAttribute(USERS_LIST_ATT, users.subList(fromIndex, endIndex));
        request.setAttribute(CURRENT_PAGE_PARAM, page);
        request.setAttribute(NB_PAGE_ATT, getNumberOfPages(users.size()));
        return JSP_BASE + DESTINATION_DISPLAY_USERS;
      }
    }
    request.setAttribute(USERS_LIST_ATT, new ArrayList<>());
    request.setAttribute(CURRENT_PAGE_PARAM, 0);
    request.setAttribute(NB_PAGE_ATT, 0);
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
