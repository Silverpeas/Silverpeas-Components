/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.components.quickinfo.notification;

import org.silverpeas.components.quickinfo.model.News;

import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;

public abstract class AbstractNewsUserNotification extends AbstractQuickInfoUserNotification<News> {

  private final NotifAction action;

  public AbstractNewsUserNotification(News resource, NotifAction action) {
    super(resource);
    this.action = action;
  }

  @Override
  protected UserDetail getSenderDetail() {
    return UserDetail.getById(getSender());
  }

  @Override
  protected void performTemplateData(String language, News resource, SilverpeasTemplate template) {
    getNotificationMetaData()
        .addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()),
            "");
    template.setAttribute("title", resource.getTitle());
    template.setAttribute("description", resource.getDescription());
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
      userId = getResource().getCreatedBy();
    }
    return userId;
  }
}