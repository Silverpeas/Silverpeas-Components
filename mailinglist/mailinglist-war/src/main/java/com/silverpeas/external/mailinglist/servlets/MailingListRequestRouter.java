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

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.mailinglist.control.MailingListSessionController;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;

public class MailingListRequestRouter extends ComponentRequestRouter implements
    MailingListRoutage {
  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "MailingList";
  }

  /**
   * Method declaration
   *
   *
   * @param mainSessionCtrl
   * @param componentContext
   *
   * @return
   *
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new MailingListSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   *
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  @SuppressWarnings("unchecked")
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String type = request.getParameter("Type");
    try {
      RestRequest rest = new RestRequest(request);
      if ("searchResult".equalsIgnoreCase(function)
          || "searchResult.jsp".equalsIgnoreCase(function)) {
        if (DESTINATION_MESSAGE.equalsIgnoreCase(type)) {
          rest.getElements().put(DESTINATION_MESSAGE,
              request.getParameter("Id"));
          rest.setComponentId(componentSC.getComponentId());
        } else if ("com.stratelia.webactiv.calendar.backbone.TodoDetail"
            .equalsIgnoreCase(type)) {
          String destination = request.getScheme() + "://"
              + request.getServerName() + ':' + request.getServerPort()
              + request.getContextPath() + request.getServletPath() + '/'
              + componentSC.getComponentId() + '/' + request.getParameter("Id");
          return destination;
        }
      }
      else if ("portlet".equalsIgnoreCase(function) || "portlet.jsp".equalsIgnoreCase(function))
      {
    	  rest.getElements().put(DESTINATION_PORTLET, "dummy");
      }
      boolean isModerator = false;
      boolean isAdmin = false;
      boolean isModerated = ((MailingListSessionController) componentSC)
          .isModerated();
      String[] roles = componentSC.getUserRoles();
      for (int i = 0; i < roles.length; i++) {
        if ("admin".equalsIgnoreCase(roles[i])) {
          isAdmin = true;
        } else if ("moderator".equalsIgnoreCase(roles[i])) {
          isModerator = true;
        }
      }

      request.setAttribute(RSS_URL_ATT,
          ((AbstractComponentSessionController) componentSC).getRSSUrl());
      request.setAttribute(IS_USER_ADMIN_ATT, new Boolean(isAdmin));
      request.setAttribute(IS_USER_MODERATOR_ATT, new Boolean(isModerator));
      request.setAttribute(IS_LIST_MODERATED_ATT, new Boolean(isModerated));
      request.setAttribute(COMPONENT_ID_ATT, componentSC.getComponentId());
      if (rest.getElements().get(DESTINATION_MESSAGE) != null) {
        return MessageProcessor.processMessage(rest, request);
      } else if (rest.getElements().get(DESTINATION_MODERATION) != null) {
        return ModerationProcessor.processModeration(rest, request);
      } else if (rest.getElements().get(DESTINATION_LIST) != null) {
        return MailingListProcessor.processMailingList(rest, request);
      } else if (rest.getElements().get(DESTINATION_USERS) != null) {
        return UsersProcessor.processUsers(rest, request);
      } else if (rest.getElements().get(DESTINATION_SUBSCRIBERS) != null) {
        return SubscribersProcessor.processSubscription(rest, request, componentSC);
      } else if (rest.getElements().get(DESTINATION_PORTLET) != null) {
        return PortletProcessor.processActivities(rest, request, componentSC.getUserId());
      }
      return ActivitiesProcessor.processActivities(rest, request, componentSC
          .getUserId());

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }
}
