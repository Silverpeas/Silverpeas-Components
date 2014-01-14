/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class UpdateTopicStatusHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, KmeliaSessionController kmelia) {
    String subTopicId = request.getParameter("Id");
    String newStatus = request.getParameter("Status");
    String recursive = request.getParameter("Recursive");

    try {
      kmelia.changeTopicStatus(newStatus, subTopicId, "1".equals(recursive));
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "UpdateTopicStatusHandler.handleRequest",
          "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }
}
