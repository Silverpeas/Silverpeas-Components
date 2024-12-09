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

import org.owasp.encoder.Encode;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

/**
 * @author Nicolas EYSSERIC
 */
public abstract class AbstractKmeliaFolderUserNotification
    extends AbstractKmeliaUserNotification<NodeDetail> {

  private final NotifAction action;

  protected AbstractKmeliaFolderUserNotification(final NodeDetail resource, final NotifAction action) {
    super(resource);
    this.action = action;
  }

  protected void performTemplateData(final String language, final NodeDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("path", getHTMLNodePath(resource.getFatherPK(), language));
    template.setAttribute("topic", resource);
    template.setAttribute("topicName", Encode.forHtml(resource.getName(language)));
    template.setAttribute("topicDescription", Encode.forHtml(resource.getDescription(language)));
  }

  @Override
  protected void performNotificationResource(final String language, final NodeDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(resource.getNodeType());
    notificationResourceData.setResourceName(resource.getName(language));
    notificationResourceData.setResourceDescription(resource.getDescription(language));
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return (!NotifAction.REPORT.equals(action));
  }

  @Override
  protected String getResourceURL(final NodeDetail resource) {
    return KmeliaHelper.getNodeUrl(resource);
  }

  protected final String getPath(final String language) {
    if (getResource().getNodePK() == null) {
      return "";
    }
    return getHTMLNodePath(getResource().getNodePK(), language);
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getNodePK().getInstanceId();
  }

  @Override
  protected String getSender() {
    if (NotifAction.REPORT.equals(action)) {
      return null;
    }
    return getResource().getCreatorId();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "kmelia.notifTopicLinkLabel";
  }

  @Override
  protected void perform(final NodeDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

}