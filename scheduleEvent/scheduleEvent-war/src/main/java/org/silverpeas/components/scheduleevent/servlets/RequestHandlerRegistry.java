/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.components.scheduleevent.servlets.handlers.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * Registry of request handlers for the request router.
 * @author mmoquillon
 */
public class RequestHandlerRegistry {

  private final HashMap<String, ScheduleEventRequestHandler> actions = new HashMap<>();

  private RequestHandlerRegistry() {

  }

  @PostConstruct
  private void setUpRegistry() {
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
  }

  public ScheduleEventRequestHandler getHandler(final String action) {
    return actions.get(action);
  }
}
  