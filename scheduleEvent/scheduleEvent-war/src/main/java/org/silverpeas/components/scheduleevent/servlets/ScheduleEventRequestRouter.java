/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.scheduleevent.servlets;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.servlets.handlers.ScheduleEventRequestHandler;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

public class ScheduleEventRequestRouter extends
    ComponentRequestRouter<ScheduleEventSessionController> {

  private static final long serialVersionUID = 368205022700081777L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "ScheduleEvent";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   *
   */
  public ScheduleEventSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ScheduleEventSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param scheduleeventSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpRequest request) {
    RequestHandlerRegistry registry = ServiceProvider.getService(RequestHandlerRegistry.class);
    String destination;
    try {
      ScheduleEventRequestHandler currentAction = registry.getHandler(function);
      if (currentAction != null) {
        destination = currentAction.getDestination(function, scheduleeventSC, request);
      } else {
        SilverLogger.getLogger(this).warn("Function " + function + " has no associated action");
        destination = "list.jsp";
      }
      if (!function.startsWith("Open")) {
        destination = "/scheduleevent/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }


    return destination;
  }
}
