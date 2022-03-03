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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.components.scheduleevent.notification;

import org.silverpeas.components.scheduleevent.service.model.beans.Contributor;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public class ScheduleEventUserNotification extends AbstractScheduleEventUserNotification {

  public ScheduleEventUserNotification(final ScheduleEvent resource,
      final UserDetail senderUserDetail) {
    super(resource, senderUserDetail);
  }

  @Override
  protected String getTemplateFileName() {
    return "new";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    final Set<Contributor> contributors = getResource().getContributors();
    final List<String> userIds = new ArrayList<String>(contributors.size());
    for (final Contributor contributor : contributors) {
      userIds.add(Integer.toString(contributor.getUserId()));
    }
    return userIds;
  }

  @Override
  protected void performTemplateData(final String language, final ScheduleEvent resource,
      final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("eventName", resource.getTitle());
    template.setAttribute("eventDescription", resource.getDescription());
    template.setAttribute("eventCreationDate",
        DateUtil.getOutputDate(resource.getCreationDate(), language));
    template.setAttribute("event", resource);
    template.setAttribute("senderName", getSenderUserDetail().getDisplayedName());
  }

  @Override
  protected void performNotificationResource(final String language, final ScheduleEvent resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
    notificationResourceData.setResourceDescription(resource.getDescription());
  }
}
