/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.notification;

import org.silverpeas.components.forums.model.Message;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * User: Yohann Chastagnier
 * Date: 10/06/13
 */
public abstract class AbstractForumsMessageUserNotification
    extends AbstractForumsUserNotification<Message> {

  private NotifAction action = null;

  /**
   * @param resource
   */
  public AbstractForumsMessageUserNotification(final Message resource) {
    super(resource);
  }

  /**
   * @param resource
   * @param action
   */
  public AbstractForumsMessageUserNotification(final Message resource, final NotifAction action) {
    super(resource);
    this.action = action;
  }

  @Override
  protected void performTemplateData(final String language, final Message resource,
      final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      SilverLogger.getLogger(this).warn(ex);
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("isSubject", resource.isSubject());
    template.setAttribute("title", resource.getTitle());
    template.setAttribute("text", resource.getText());
    template.setAttribute("originTitle", getForumsService()
        .getMessageTitle(getForumsService().getMessageParentId(getResource().getId())));
  }

  @Override
  protected void performNotificationResource(final String language, final Message resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setFeminineGender(false);
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(resource.getResourceType());
    notificationResourceData.setResourceName(resource.getTitle());
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getResourceURL(final Message resource) {
    final int initialSize = 2;
    Map<String, String> params = new HashMap<>(initialSize);
    params.put("componentId", getComponentInstanceId());
    params.put("messageId", resource.getIdAsString());
    return StringUtil.format(settings.getString("forums.message.link"), params);
  }

  /**
   * Gets the bundle key prefix according to the resource if it is a subject or a message.
   * @return
   */
  protected String getNotificationBundleKeyPrefix() {
    StringBuilder bundleKeyPrefix = new StringBuilder("forums.");
    if (getResource().isSubject()) {
      bundleKeyPrefix.append("subject");
    } else {
      bundleKeyPrefix.append("message");
    }
    return bundleKeyPrefix.append(".notification.").toString();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return getNotificationBundleKeyPrefix() + "notifLinkLabel";
  }
}
