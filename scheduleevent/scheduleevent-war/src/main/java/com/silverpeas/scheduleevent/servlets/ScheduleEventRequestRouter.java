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

package com.silverpeas.scheduleevent.servlets;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventAddDateRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventAddInfosGeneRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventAddOptionsHourRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventAddOptionsNextRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventAddRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventBackDateRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventBackHourRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventBackInfosGeneRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventCancelRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventConfirmRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventConfirmScreenRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventConfirmUsersRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDeleteDateRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDeleteRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDetailRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventMainRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventModifyStateRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventOpenUserRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventValidResponseRequestHandler;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ScheduleEventRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 368205022700081777L;

  private static final HashMap<String, ScheduleEventRequestHandler> actions =
      new HashMap<String, ScheduleEventRequestHandler>();
  static {
    actions.put("Main", new ScheduleEventMainRequestHandler());
    actions.put("Detail", new ScheduleEventDetailRequestHandler());
    actions.put("Add", new ScheduleEventAddRequestHandler());
    actions.put("Delete", new ScheduleEventDeleteRequestHandler());
    actions.put("Cancel", new ScheduleEventCancelRequestHandler());
    actions.put("AddInfoGene", new ScheduleEventAddInfosGeneRequestHandler());
    actions.put("BackInfoGene", new ScheduleEventBackInfosGeneRequestHandler());
    actions.put("AddDate", new ScheduleEventAddDateRequestHandler());
    actions.put("DeleteDate", new ScheduleEventDeleteDateRequestHandler());
    actions.put("AddOptionsNext", new ScheduleEventAddOptionsNextRequestHandler());
    actions.put("AddOptionsHour", new ScheduleEventAddOptionsHourRequestHandler());
    actions.put("BackDate", new ScheduleEventBackDateRequestHandler());
    actions.put("BackHour", new ScheduleEventBackHourRequestHandler());
    actions.put("OpenUserPopup", new ScheduleEventOpenUserRequestHandler());
    actions.put("ConfirmScreen", new ScheduleEventConfirmScreenRequestHandler());
    actions.put("ConfirmUsers", new ScheduleEventConfirmUsersRequestHandler());
    actions.put("Confirm", new ScheduleEventConfirmRequestHandler());
    actions.put("ModifyState", new ScheduleEventModifyStateRequestHandler());
    actions.put("ValidResponse", new ScheduleEventValidResponseRequestHandler());
  };

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
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ScheduleEventSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";
    ScheduleEventSessionController scheduleeventSC = (ScheduleEventSessionController) componentSC;
    SilverTrace.info("scheduleevent", "ScheduleEventRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);

    try {
      ScheduleEventRequestHandler currentAction = actions.get(function);
      if (currentAction != null) {
        destination = currentAction.getDestination(function, scheduleeventSC, request);
      } else {
        SilverTrace.warn("scheduleevent", "ScheduleEventRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "Function " + function + " has not an action associated");
        destination = "list.jsp";
      }
      if (!function.startsWith("Open")) {
        destination = "/scheduleevent/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("scheduleevent", "ScheduleEventRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

}
