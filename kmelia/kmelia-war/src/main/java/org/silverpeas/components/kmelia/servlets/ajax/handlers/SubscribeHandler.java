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
package org.silverpeas.components.kmelia.servlets.ajax.handlers;

import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.servlets.ajax.AjaxHandler;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;

public class SubscribeHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, KmeliaSessionController kmelia) {
    String topicId = request.getParameter("Id");
    try {
      // check if user is allowed to access to given topic
      if (isNodeAvailable(kmelia, topicId)) {
        kmelia.addSubscription(topicId);
        return "ok";
      }
      return "nok";
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      return e.getMessage();
    }
  }

  private boolean isNodeAvailable(KmeliaSessionController kmelia, String nodeId) {
    return NodeAccessControl.get().isUserAuthorized(kmelia.getUserId(), new NodePK(nodeId, kmelia.getComponentId()));
  }
}
