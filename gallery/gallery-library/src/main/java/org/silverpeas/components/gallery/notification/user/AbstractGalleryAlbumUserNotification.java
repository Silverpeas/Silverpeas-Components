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

package org.silverpeas.components.gallery.notification.user;

import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

import java.util.MissingResourceException;

import static org.silverpeas.components.gallery.model.AlbumDetail.RESOURCE_TYPE;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.WebEncodeHelper.javaStringToHtmlString;

/**
 * Centralizes the building of a {@link UserNotification} in relation with an album.
 * @author silveryocha
 */
public abstract class AbstractGalleryAlbumUserNotification
    extends AbstractGalleryUserNotification<AlbumDetail> {

  private final User sender;

  AbstractGalleryAlbumUserNotification(final AlbumDetail resource, final User sender) {
    super(resource);
    this.sender = sender;
  }

  @Override
  protected void perform(final AlbumDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getTemplatePath() {
    return "gallery";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getNodePK().getInstanceId();
  }

  @Override
  protected String getSender() {
    return sender.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.gallery.multilang.galleryBundle";
  }

  @Override
  protected String getResourceURL(final AlbumDetail resource) {
    final String applicationURL = URLUtil.getApplicationURL();
    return defaultStringIfNotDefined(resource.getPermalink(), applicationURL)
        .substring(applicationURL.length());
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "gallery.album.link.label";
  }

  @Override
  protected void performTemplateData(final String language, final AlbumDetail resource,
      final SilverpeasTemplate template) {
    String title = getTitleByLanguage(language);
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("path", getPath(language));
    template.setAttribute("album", resource);
    template.setAttribute("albumName", resource.getName(language));
    template.setAttribute("albumDesc", resource.getDescription(language));
  }

  @Override
  protected void performNotificationResource(final String language, final AlbumDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setFeminineGender(false);
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(RESOURCE_TYPE);
    notificationResourceData.setResourceName(resource.getName(language));
    notificationResourceData.setResourceDescription(resource.getDescription(language));
  }

  private String getTitleByLanguage(final String language) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      title = getTitle();
    }
    return title;
  }

  protected final String getPath(final String language) {
    final NodePK nodePK = getResource().getNodePK();
    return javaStringToHtmlString(NodeService.get().getPath(nodePK).format(language));
  }
}
