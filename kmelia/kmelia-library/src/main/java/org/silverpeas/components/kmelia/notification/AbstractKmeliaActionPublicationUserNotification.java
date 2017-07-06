/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.components.kmelia.notification;

import static org.silverpeas.core.util.StringUtil.isDefined;

import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractKmeliaActionPublicationUserNotification extends AbstractKmeliaPublicationUserNotification {

  public AbstractKmeliaActionPublicationUserNotification(NodePK nodePK, PublicationDetail resource, NotifAction action) {
    super(nodePK, resource, action);
  }

  @Override
  protected String getSender() {
    return getMostRecentPublicationUpdater();
  }

  /**
   * Gets the most recent identifier of the user which performed the last modification.<br/>
   * If no identifier is retrieved, {@link #stop()} method is called and the process of
   * notification building is terminated.
   * @return a user identifier as string.
   */
  String getMostRecentPublicationUpdater() {
    String userId = getResource().getUpdaterId();
    if (!isDefined(userId)) {
      userId = getResource().getCreatorId();
    }
    if (!isDefined(userId)) {
      stop();
    }
    return userId;
  }
}
