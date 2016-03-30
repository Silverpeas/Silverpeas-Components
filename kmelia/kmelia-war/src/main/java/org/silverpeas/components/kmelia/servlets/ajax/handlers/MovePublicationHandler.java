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
package org.silverpeas.components.kmelia.servlets.ajax.handlers;


import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.components.kmelia.servlets.ajax.AjaxHandler;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

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

  public KmeliaService getKmeliaBm() {
    try {
      return ServiceProvider.getService(KmeliaService.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("MovePublicationHandler.getKmeliaService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}