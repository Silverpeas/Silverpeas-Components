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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.components.kmelia.servlets.ajax.AjaxHandler;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.util.JSONCodec;

import javax.servlet.http.HttpServletRequest;

import static org.silverpeas.components.kmelia.KmeliaPublicationHelper.isCanBeCut;
import static org.silverpeas.components.kmelia.KmeliaPublicationHelper.isRemovable;

public class GetPublicationAuthorizationsHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, KmeliaSessionController kmelia) {
    final String pubId = request.getParameter("pubId");
    final String instanceId = kmelia.getComponentId();
    final PublicationPK pubPK = new PublicationPK(pubId, instanceId);
    final PublicationDetail publication = KmeliaService.get().getPublicationDetail(pubPK);
    final String nodeId = request.getParameter("nodeId");
    final String currentUserId = kmelia.getUserId();
    final String nodeProfile = kmelia.getUserTopicProfile(nodeId);
    final User pubOwner = publication.getCreator();
    return JSONCodec.encodeObject(j -> {
      j.put("canBeCut", isCanBeCut(instanceId, currentUserId, nodeProfile, pubOwner));
      j.put("canBeDeleted", isRemovable(instanceId, currentUserId, nodeProfile, pubOwner));
      return j;
    });
  }
}
