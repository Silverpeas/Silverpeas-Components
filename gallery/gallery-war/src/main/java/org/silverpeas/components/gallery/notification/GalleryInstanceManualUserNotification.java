/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.gallery.notification;

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.notification.user.GalleryUserAlertNotification;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;

import javax.inject.Named;

import static org.silverpeas.components.gallery.service.MediaServiceProvider.getMediaService;

/**
 * @author silveryocha
 */
@Named
public class GalleryInstanceManualUserNotification extends
    AbstractComponentInstanceManualUserNotification {

  private static final String MEDIA_KEY = "MediaKey";

  @Override
  protected boolean check(final NotificationContext context) {
    final String componentId = context.getComponentId();
    final String mediaId = context.getContributionId();
    final Media media = getMedia(componentId, mediaId);
    context.put(MEDIA_KEY, media);
    return media.canBeAccessedBy(context.getSender());
  }

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final Media media = context.getObject(MEDIA_KEY);
    final String componentId = context.getComponentId();
    final String currentAlbumId = context.getNodeId();
    final NodePK nodePK = new NodePK(currentAlbumId, componentId);
    return new GalleryUserAlertNotification(nodePK, media, context.getSender()).build();
  }

  private Media getMedia(final String instanceId, final String mediaId) {
    return getMediaService().getMedia(new MediaPK(mediaId, instanceId));
  }
}
