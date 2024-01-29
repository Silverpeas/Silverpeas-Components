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

package org.silverpeas.components.gallery.notification.user;

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;
import java.util.Collections;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.WebEncodeHelper.javaStringToHtmlString;

/**
 * @author silveryocha
 */
public class GalleryUserAlertNotification extends AbstractGalleryUserNotification<Media> {

  private final NodePK nodePK;
  private final User sender;

  public GalleryUserAlertNotification(final NodePK nodePK, final Media resource, final User sender) {
    super(resource);
    this.nodePK = nodePK;
    this.sender = sender;
  }

  @Override
  protected String getTemplateFileName() {
    return "manualUserNotification";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "gallery.notifLinkLabel";
  }

  @Override
  protected void perform(final Media resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected void performTemplateData(final String language, final Media resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData()
        .addLanguage(language, defaultStringIfNotDefined(getTitle(language), getTitle()), "");
    template.setAttribute("senderName", sender.getDisplayedName());
    template.setAttribute("mediaTitle", resource.getTitle());
    template.setAttribute("path", getPath(language));
  }

  @Override
  protected void performNotificationResource(final String language, final Media resource,
      final NotificationResourceData notificationResourceData) {
    // Nothing to do here
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return sender.getId();
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    // Users to notify are not handled here.
    return Collections.emptyList();
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return false;
  }

  private String getPath(final String language) {
    return javaStringToHtmlString(NodeService.get().getPath(nodePK).format(language));
  }
}
