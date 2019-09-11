/*
 * Copyright (C) 2000 - 2019 Silverpeas
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


import org.silverpeas.components.kmelia.KmeliaPasteDetail;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.components.kmelia.servlets.ajax.AjaxHandler;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;

public class MovePublicationHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, KmeliaSessionController controller) {
    String id = request.getParameter("Id");
    String sourceId = request.getParameter("SourceNodeId");
    String targetId = request.getParameter("TargetNodeId");
    String validatorIds = request.getParameter("ValidatorIds");
    try {
      PublicationPK pubPK = new PublicationPK(id, controller.getComponentId());
      NodePK from = new NodePK(sourceId, controller.getComponentId());
      NodePK to = new NodePK(targetId, controller.getComponentId());
      KmeliaPasteDetail pasteDetail = new KmeliaPasteDetail(to);
      pasteDetail.setFromPK(from);
      pasteDetail.setUserId(controller.getUserId());
      pasteDetail.setTargetValidatorIds(validatorIds);
      getKmeliaService().movePublicationInSameApplication(pubPK, from, pasteDetail);
      return "ok";
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e, controller.getLanguage());
      return e.getMessage();
    }
  }

  public KmeliaService getKmeliaService() {
    return ServiceProvider.getService(KmeliaService.class);
  }
}