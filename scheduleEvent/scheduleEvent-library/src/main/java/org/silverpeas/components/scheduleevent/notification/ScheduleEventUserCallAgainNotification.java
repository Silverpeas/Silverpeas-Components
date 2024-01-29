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
package org.silverpeas.components.scheduleevent.notification;

import org.silverpeas.components.scheduleevent.service.model.beans.Contributor;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import static java.lang.String.valueOf;

/**
 * @author Yohann Chastagnier
 */
public class ScheduleEventUserCallAgainNotification extends AbstractScheduleEventUserNotification {

  private String message;

  public ScheduleEventUserCallAgainNotification(final ScheduleEvent resource, String message,
      final UserDetail senderUserDetail) {
    super(resource, senderUserDetail);
    this.message = message;
  }

  @Override
  protected String getTemplateFileName() {
    return "callagain";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    final Set<Contributor> contributors = getResource().getContributors();
    final List<String> userIdsToNotify = new ArrayList<String>(contributors.size());

    // First getting potential users to notify
    for (final Contributor contributor : contributors) {
      userIdsToNotify.add(valueOf(contributor.getUserId()));
    }

    // Then excluding those that have given a response
    for (Response response : getResource().getResponses()) {
      userIdsToNotify.remove(valueOf(response.getUserId()));
    }

    return userIdsToNotify;
  }

  @Override
  protected void performTemplateData(final String language, final ScheduleEvent resource,
      final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      SilverLogger.getLogger(this).silent(ex);
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("eventName", resource.getTitle());
    template.setAttribute("senderName", getSenderUserDetail().getDisplayedName());
    template.setAttribute("silverpeasURL", getResourceURL(resource));
    template.setAttribute("message", message);
  }

  @Override
  protected void performNotificationResource(final String language, final ScheduleEvent resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
    notificationResourceData.setResourceDescription(resource.getDescription());
  }
}
