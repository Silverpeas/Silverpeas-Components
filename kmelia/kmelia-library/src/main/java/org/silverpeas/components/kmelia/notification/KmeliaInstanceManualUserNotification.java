/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.notification;

import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Named;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
@Named
public class KmeliaInstanceManualUserNotification extends
    AbstractComponentInstanceManualUserNotification {

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final UserNotification notification;
    final String componentId = context.getComponentId();
    final String folderId = context.getNodeId();
    final String pubId = context.getPublicationId();
    if (!NodePK.ROOT_NODE_ID.equals(folderId)) {
      final NodeDetail node = getKmeliaService().getNodeHeader(folderId, componentId);
      if (node.haveRights()) {
        context.put(NotificationContext.RESOURCE_ID, asResourceId(node.getRightsDependsOn()));
      }
    }
    if (isDefined(pubId)) {
      final String docId = context.get("docId");
      if (isDefined(docId)) {
        notification = getUserNotification(componentId, folderId, pubId, docId);
      } else {
        notification = getUserNotification(componentId, folderId, pubId);
      }
    } else {
      notification = getUserNotification(componentId, folderId);
    }
    return notification;
  }

  protected NodePK getNodePK(final String cmpId, final String nodeId) {
    return StringUtil.isDefined(nodeId) ? new NodePK(nodeId, cmpId) : null;
  }

  private UserNotification getUserNotification(final String cmpId, final String nodeId,
      final String pubId, final String docId) {
    final NodePK nodePK = getNodePK(cmpId, nodeId);
    final PublicationPK pubPk = new PublicationPK(pubId, cmpId);
    SimpleDocumentPK documentPk = new SimpleDocumentPK(docId, cmpId);
    return getKmeliaService().getUserNotification(pubPk, documentPk, nodePK);
  }
  private UserNotification getUserNotification(final String cmpId, final String nodeId,
      final String pubId) {
    final NodePK nodePK = getNodePK(cmpId, nodeId);
    final PublicationPK pubPk = new PublicationPK(pubId, cmpId);
    return getKmeliaService().getUserNotification(pubPk, nodePK);
  }

  private UserNotification getUserNotification(final String cmpId, final String nodeId) {
    final NodePK nodePK = new NodePK(nodeId, cmpId);
    return getKmeliaService().getUserNotification(nodePK);
  }

  private KmeliaService getKmeliaService() {
    return KmeliaService.get();
  }

  private String asResourceId(final String folderId) {
    return ProfiledObjectType.NODE.getCode() + folderId;
  }
}
