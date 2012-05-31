/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.scheduleevent.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

/**
 * @author Yohann Chastagnier
 */
public class ScheduleEventUserNotification extends AbstractTemplateUserNotificationBuilder<ScheduleEvent> {

  private final UserDetail senderUserDetail;
  private final String type;

  public ScheduleEventUserNotification(final ScheduleEvent resource, final UserDetail senderUserDetail,
      final String type) {
    super(resource, null, null);
    this.senderUserDetail = senderUserDetail;
    this.type = type;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "com.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "scheduleevent";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "scheduleEvent.notifSubject";
  }

  @Override
  protected String getFileName() {
    if ("create".equals(type)) {
      return "new";
    }
    return "";
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
    getNotification().addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()), "");
    template.setAttribute("eventName", resource.getTitle());
    template.setAttribute("eventDescription", resource.getDescription());
    template.setAttribute("eventCreationDate", DateUtil.getOutputDate(resource.getCreationDate(), language));
    template.setAttribute("event", resource);
    template.setAttribute("senderName", senderUserDetail.getDisplayedName());
    template.setAttribute("silverpeasURL", getResourceURL(resource));
  }

  @Override
  protected void performNotificationResource(final String language, final ScheduleEvent resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return senderUserDetail.getId();
  }
}
