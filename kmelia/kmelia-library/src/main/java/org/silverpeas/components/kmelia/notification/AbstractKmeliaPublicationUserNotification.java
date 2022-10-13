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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.StringUtil;

import java.util.MissingResourceException;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractKmeliaPublicationUserNotification
    extends AbstractKmeliaUserNotification<PublicationDetail> {

  private final NodePK nodePK;
  private final NotifAction action;

  protected AbstractKmeliaPublicationUserNotification(final NodePK nodePK,
      final PublicationDetail resource, final NotifAction action) {
    super(resource);
    this.nodePK = nodePK;
    this.action = action;
  }

  @Override
  protected boolean isUserCanBeNotified(final String userId) {
    if (nodePK != null) {
      return NodeAccessControl.get().isUserAuthorized(userId, nodePK);
    } else {
      return ComponentAccessControl.get().isUserAuthorized(userId, getComponentInstanceId());
    }
  }

  @Override
  protected boolean isGroupCanBeNotified(final String groupId) {
    if (nodePK != null) {
      return NodeAccessControl.get().isGroupAuthorized(groupId, nodePK);
    } else {
      return ComponentAccessControl.get().isGroupAuthorized(groupId, getComponentInstanceId());
    }
  }

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("path", getPath(language));
    template.setAttribute("publication", resource);
    template.setAttribute("publicationName", resource.getName(language));
    template.setAttribute("publicationDesc", resource.getDescription(language));
    template.setAttribute("publicationKeywords", resource.getKeywords(language));
    String senderId = getSender();
    if (StringUtil.isDefined(senderId)) {
      User sender = User.getById(senderId);
      if (sender != null) {
        template.setAttribute("sender", sender);
        template.setAttribute("senderName", sender.getDisplayedName());
      }
    }
  }

  @Override
  protected void performNotificationResource(final String language,
      final PublicationDetail resource, final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getName(language));
    notificationResourceData.setResourceDescription(resource.getDescription(language));
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return (!NotifAction.REPORT.equals(action));
  }

  @Override
  protected String getResourceURL(final PublicationDetail resource) {
    return KmeliaHelper.getPublicationUrl(resource, getNodePK());
  }

  protected NodePK getNodePK() {
    return nodePK;
  }

  protected final String getPath(final String language) {
    if (nodePK == null) {
      return "";
    }
    return getHTMLNodePath(nodePK, language);
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getNodePK() != null ? getNodePK().getInstanceId() : getResource().getInstanceId();
  }

  @Override
  protected String getSender() {
    if (NotifAction.REPORT.equals(action)) {
      return null;
    } else if (NotifAction.CREATE.equals(action)) {
      String userId = getResource().getCreatorId();
      UserDetail creator = UserDetail.getById(userId);
      if (!creator.isActivatedState()) {
        return getResource().getUpdaterId();
      }
      return userId;
    }
    return getResource().getUpdaterId();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "kmelia.notifPublicationLinkLabel";
  }
}