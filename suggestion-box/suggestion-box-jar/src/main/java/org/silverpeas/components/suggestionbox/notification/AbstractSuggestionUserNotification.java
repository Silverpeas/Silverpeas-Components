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
package org.silverpeas.components.suggestionbox.notification;

import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import org.silverpeas.components.suggestionbox.model.Suggestion;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractSuggestionUserNotification
    extends AbstractSuggestionBoxUserNotification<Suggestion> {

  private final NotifAction action;

  protected AbstractSuggestionUserNotification(final Suggestion resource,
      final NotifAction action) {
    super(resource);
    this.action = action;
  }

  @Override
  protected void performTemplateData(final String language, final Suggestion resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData()
        .addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()),
            "");
    template.setAttribute("title", resource.getTitle());
    template.setAttribute("content", resource.getContent());
    template.setAttribute("senderName", getSenderName());
    template.setAttribute("silverpeasURL", getResourceURL(resource));
  }

  @Override
  protected void performNotificationResource(final String language, final Suggestion resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return (!NotifAction.REPORT.equals(action));
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getSuggestionBox().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    if (NotifAction.REPORT.equals(action)) {
      return null;
    }
    return getResource().getCreatedBy();
  }
}
