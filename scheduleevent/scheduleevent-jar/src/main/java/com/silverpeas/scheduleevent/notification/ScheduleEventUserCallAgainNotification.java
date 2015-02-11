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
package com.silverpeas.scheduleevent.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

/**
 * @author Yohann Chastagnier
 */
public class ScheduleEventUserCallAgainNotification extends
    AbstractTemplateUserNotificationBuilder<ScheduleEvent> {

  private final UserDetail senderUserDetail;
  private final String type;
  private String message;

  public ScheduleEventUserCallAgainNotification(final ScheduleEvent resource,
      String message, final UserDetail senderUserDetail,
      final String type) {
    super(resource, null, null);
    this.senderUserDetail = senderUserDetail;
    this.type = type;
    this.message = message;
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
    return "scheduleevent.notifSubject";
  }

  @Override
  protected String getFileName() {
    if ("callagain".equals(type)) {
      return "callagain";
    }
    return "";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    final Set<Contributor> contributors = getResource().getContributors();
    final List<String> userIds = new ArrayList<String>(contributors.size());
    for (final Contributor contributor : contributors) {
      int userId = contributor.getUserId();
      Iterator<Response> responses = getResource().getResponses().iterator();
      boolean found = false;
      while (responses.hasNext() && !found) {
        Response response = responses.next();
        found = response.getUserId() == userId;
      }
      if (!found) {
        userIds.add(Integer.toString(userId));
      }
    }
    return userIds;
  }

  @Override
  protected void performTemplateData(final String language, final ScheduleEvent resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language,
        getBundle(language).getString(getBundleSubjectKey(), getTitle()), "");
    template.setAttribute("eventName", resource.getTitle());
    template.setAttribute("senderName", senderUserDetail.getDisplayedName());
    template.setAttribute("silverpeasURL", getResourceURL(resource));
    template.setAttribute("message", message);
    template.setAttribute("hasmessage", StringUtils.isNotBlank(message));
  }

  @Override
  protected void performNotificationResource(final String language, final ScheduleEvent resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
    notificationResourceData.setResourceDescription(resource.getDescription());
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
