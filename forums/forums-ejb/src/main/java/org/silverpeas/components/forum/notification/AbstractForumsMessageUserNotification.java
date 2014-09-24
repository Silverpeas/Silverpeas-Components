/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.forum.notification;

import com.silverpeas.notification.model.NotificationResourceData;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.webactiv.forums.models.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 10/06/13
 */
public abstract class AbstractForumsMessageUserNotification
    extends AbstractForumsUserNotification<Message> {

  /**
   * Default constructor.
   * @param resource
   */
  public AbstractForumsMessageUserNotification(final Message resource) {
    super(resource);
  }

  @Override
  protected void performTemplateData(final String language, final Message resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData()
        .addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()),
            "");
    template.setAttribute("isSubject", resource.isSubject());
    template.setAttribute("title", resource.getTitle());
    template.setAttribute("text", resource.getText());
    template.setAttribute("originTitle",
        getForumsBm().getMessageTitle(getForumsBm().getMessageParentId(getResource().getId())));
  }

  @Override
  protected void performNotificationResource(final String language, final Message resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(resource.getResourceType());
    notificationResourceData.setResourceName(resource.getTitle());
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getResourceURL(final Message resource) {
    Map<String, String> params = new HashMap<String, String>(2);
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
}
