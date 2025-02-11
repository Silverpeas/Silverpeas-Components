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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo.notification;

import org.owasp.encoder.Encode;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.kernel.util.StringUtil;

public abstract class AbstractNewsUserNotification extends AbstractQuickInfoUserNotification<News> {

  private final NotifAction action;

  public AbstractNewsUserNotification(News resource, NotifAction action) {
    super(resource);
    this.action = action;
  }

  @Override
  protected void performTemplateData(String language, News resource, SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("title", Encode.forHtml(resource.getTitle()));
    template.setAttribute("description", Encode.forHtml(resource.getDescription()));
    template.setAttribute("authorName", resource.getCreator().getDisplayedName());
    template.setAttribute("senderName", getSenderName());
  }

  @Override
  protected void performNotificationResource(String language, News resource,
      NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    String userId = getResource().getUpdaterId();
    if (!StringUtil.isDefined(userId)) {
      userId = getResource().getCreatorId();
    }
    return userId;
  }
}