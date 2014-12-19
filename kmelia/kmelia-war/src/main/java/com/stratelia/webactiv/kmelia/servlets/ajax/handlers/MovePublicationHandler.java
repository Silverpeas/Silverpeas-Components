/**
 * Copyright (C) 2000 - 2014 Silverpeas
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
package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;


import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.servlet.http.HttpServletRequest;

public class MovePublicationHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, KmeliaSessionController controller) {
    String id = request.getParameter("Id");
    String sourceId = request.getParameter("SourceNodeId");
    String targetId = request.getParameter("TargetNodeId");
    try {
      PublicationPK pubPK = new PublicationPK(id, controller.getComponentId());
      NodePK from = new NodePK(sourceId, controller.getComponentId());
      NodePK to = new NodePK(targetId, controller.getComponentId());
      getKmeliaBm().movePublicationInSameApplication(pubPK, from, to, controller.getUserId());
      return "ok";
    } catch (Exception e) {
      SilverTrace.error("kmelia", "PasteHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e, controller.getLanguage());
      return e.getMessage();
    }
  }

  public KmeliaBm getKmeliaBm() {
    try {
      return ServiceProvider.getService(KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("MovePublicationHandler.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}