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

package com.silverpeas.scheduleevent.servlets;

import java.util.HashMap;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventAddDateRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventAddRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventBackwardRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventCallAgainRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventCancelRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventConfirmRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventConfirmUsersRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDateNextRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDeleteDateRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDeleteRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDescriptionNextRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventDetailRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventExportRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventMainRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventModifyStateRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventOpenUserRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventSimpleFormRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventTimeNextRequestHandler;
import com.silverpeas.scheduleevent.servlets.handlers.ScheduleEventValidResponseRequestHandler;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import org.silverpeas.servlet.HttpRequest;

public class ScheduleEventRequestRouter extends
    ComponentRequestRouter<ScheduleEventSessionController> {

  private static final long serialVersionUID = 368205022700081777L;

  private static final HashMap<String, ScheduleEventRequestHandler> actions =
      new HashMap<String, ScheduleEventRequestHandler>();
  static {
    ScheduleEventMainRequestHandler mainRequestHandler =
        new ScheduleEventMainRequestHandler("list.jsp");
    ScheduleEventDeleteRequestHandler deleteRequestHandler =
        new ScheduleEventDeleteRequestHandler();
    ScheduleEventModifyStateRequestHandler modifyStateRequestHandler =
        new ScheduleEventModifyStateRequestHandler();
    ScheduleEventDetailRequestHandler detailRequestHandler =
        new ScheduleEventDetailRequestHandler("detail.jsp");
    ScheduleEventValidResponseRequestHandler validResponseRequestHandler =
        new ScheduleEventValidResponseRequestHandler();
    ScheduleEventCancelRequestHandler cancelRequestHandler =
        new ScheduleEventCancelRequestHandler();
    ScheduleEventAddRequestHandler addRequestHandler =
        new ScheduleEventAddRequestHandler("form/generalInfo.jsp");
    ScheduleEventDescriptionNextRequestHandler infoNextRequestHandler =
        new ScheduleEventDescriptionNextRequestHandler();
    ScheduleEventBackwardRequestHandler infoBackRequestHandler =
        new ScheduleEventBackwardRequestHandler();
    ScheduleEventSimpleFormRequestHandler dateRequestHandler =
        new ScheduleEventSimpleFormRequestHandler("form/options.jsp");
    ScheduleEventDeleteDateRequestHandler dateDeleteRequestHandler =
        new ScheduleEventDeleteDateRequestHandler();
    ScheduleEventAddDateRequestHandler dateAddRequestHandler =
        new ScheduleEventAddDateRequestHandler();
    ScheduleEventDateNextRequestHandler dateNextRequestHandler =
        new ScheduleEventDateNextRequestHandler();
    ScheduleEventSimpleFormRequestHandler timeRequestHandler =
        new ScheduleEventSimpleFormRequestHandler("form/optionshour.jsp");
    ScheduleEventBackwardRequestHandler dateBackRequestHandler =
        new ScheduleEventBackwardRequestHandler();
    ScheduleEventTimeNextRequestHandler timeNextRequestHandler =
        new ScheduleEventTimeNextRequestHandler();
    ScheduleEventBackwardRequestHandler timeBackRequestHandler =
        new ScheduleEventBackwardRequestHandler();
    ScheduleEventSimpleFormRequestHandler notifyRequestHandler =
        new ScheduleEventSimpleFormRequestHandler("form/notify.jsp");
    ScheduleEventConfirmRequestHandler confirmRequestHandler =
        new ScheduleEventConfirmRequestHandler();
    ScheduleEventConfirmUsersRequestHandler confirmUsersRequestHandler =
        new ScheduleEventConfirmUsersRequestHandler(true);
    ScheduleEventConfirmUsersRequestHandler modifyUsersRequestHandler =
        new ScheduleEventConfirmUsersRequestHandler(false);
    ScheduleEventExportRequestHandler exportRequestHandler =
        new ScheduleEventExportRequestHandler("exportIcalPopup.jsp");
    ScheduleEventCallAgainRequestHandler callAgainRequestHandler =
        new ScheduleEventCallAgainRequestHandler();

    deleteRequestHandler.setForwardRequestHandler(mainRequestHandler);
    modifyStateRequestHandler.setForwardRequestHandler(mainRequestHandler);

    cancelRequestHandler.setForwardRequestHandler(mainRequestHandler);

    infoBackRequestHandler.setBackRequestHandler(addRequestHandler);
    infoNextRequestHandler.setFirstStepRequestHandler(addRequestHandler);
    infoNextRequestHandler.setForwardRequestHandler(dateRequestHandler);

    dateBackRequestHandler.setBackRequestHandler(dateRequestHandler);
    dateAddRequestHandler.setForwardRequestHandler(dateRequestHandler);
    dateDeleteRequestHandler.setForwardRequestHandler(dateRequestHandler);
    dateNextRequestHandler.setFirstStepRequestHandler(addRequestHandler);
    dateNextRequestHandler.setForwardRequestHandler(timeRequestHandler);

    timeBackRequestHandler.setBackRequestHandler(timeRequestHandler);
    timeNextRequestHandler.setFirstStepRequestHandler(addRequestHandler);
    timeNextRequestHandler.setForwardRequestHandler(notifyRequestHandler);

    confirmUsersRequestHandler.setForwardRequestHandler(notifyRequestHandler);
    modifyUsersRequestHandler.setForwardRequestHandler(detailRequestHandler);
    confirmRequestHandler.setForwardRequestHandler(mainRequestHandler);

    validResponseRequestHandler.setForwardRequestHandler(detailRequestHandler);

    callAgainRequestHandler.setForwardRequestHandler(detailRequestHandler);

    actions.put("Main", mainRequestHandler);
    actions.put("Detail", detailRequestHandler);
    actions.put("ValidResponse", validResponseRequestHandler);
    actions.put("Add", addRequestHandler);
    actions.put("Delete", deleteRequestHandler);
    actions.put("ModifyState", modifyStateRequestHandler);
    actions.put("ExportToICal", exportRequestHandler);

    actions.put("Cancel", cancelRequestHandler);

    actions.put("BackInfoGene", infoBackRequestHandler);
    actions.put("AddInfoGene", infoNextRequestHandler);

    actions.put("BackDate", dateBackRequestHandler);
    actions.put("AddDate", dateAddRequestHandler);
    actions.put("DeleteDate", dateDeleteRequestHandler);
    actions.put("AddOptionsNext", dateNextRequestHandler);

    actions.put("BackHour", timeBackRequestHandler);
    actions.put("AddOptionsHour", timeNextRequestHandler);

    actions.put("OpenUserPopup", new ScheduleEventOpenUserRequestHandler());
    actions.put("ConfirmScreen", notifyRequestHandler);
    actions.put("ConfirmUsers", confirmUsersRequestHandler);
    actions.put("ConfirmModifyUsers", modifyUsersRequestHandler);
    actions.put("Confirm", confirmRequestHandler);
    actions.put("CallAgain", callAgainRequestHandler);
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
    String destination = "";
    SilverTrace
        .info("scheduleevent", "ScheduleEventRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "User=" + scheduleeventSC.getUserId() + " Function=" +
                function);

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
