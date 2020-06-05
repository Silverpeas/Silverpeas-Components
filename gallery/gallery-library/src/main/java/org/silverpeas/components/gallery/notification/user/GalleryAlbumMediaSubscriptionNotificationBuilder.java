/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.components.gallery.notification.user;

import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.RemoveSenderRecipientBehavior;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.core.subscription.service.ResourceSubscriptionProvider.getSubscribersOfSubscriptionResource;

/**
 * A builder of notifications to gallery subscribers to inform them about some changes in an album.
 * @author silveryocha
 */
public class GalleryAlbumMediaSubscriptionNotificationBuilder
    extends AbstractGalleryAlbumUserNotification implements RemoveSenderRecipientBehavior,
    UserSubscriptionNotificationBehavior {

  private final NotifAction notificationCause;
  private SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes;
  List<Media> concernedMedia = new ArrayList<>();

  GalleryAlbumMediaSubscriptionNotificationBuilder(final AlbumDetail resource, final User sender) {
    super(resource, sender);
    notificationCause = NotifAction.POPULATED;
  }

  GalleryAlbumMediaSubscriptionNotificationBuilder aboutMedia(final Media... medias) {
    concernedMedia.addAll(Arrays.asList(medias));
    return this;
  }

  @Override
  protected NotifAction getAction() {
    return notificationCause;
  }

  @Override
  protected String getTemplateFileName() {
    return "galleryAlbumMediaAdded";
  }

  @Override
  protected void performTemplateData(final String language, final AlbumDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("severalMedia", concernedMedia.size() > 1);
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return getSubscriberIdsByTypes().get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return getSubscriberIdsByTypes().get(SubscriberType.GROUP).getAllIds();
  }

  private SubscriptionSubscriberMapBySubscriberType getSubscriberIdsByTypes() {
    if (subscriberIdsByTypes == null) {
      subscriberIdsByTypes = getSubscribersOfSubscriptionResource(
          NodeSubscriptionResource.from(getResource().getNodePK())).indexBySubscriberType();
    }
    return subscriberIdsByTypes;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "gallery.media.subscription.subject";
  }
}
