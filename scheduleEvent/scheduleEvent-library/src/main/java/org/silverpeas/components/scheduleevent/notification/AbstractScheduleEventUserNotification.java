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
package org.silverpeas.components.scheduleevent.notification;

import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractScheduleEventUserNotification
    extends AbstractTemplateUserNotificationBuilder<ScheduleEvent> {

  private final UserDetail senderUserDetail;

  protected AbstractScheduleEventUserNotification(final ScheduleEvent resource,
      final UserDetail senderUserDetail) {
    super(resource);
    this.senderUserDetail = senderUserDetail;
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "scheduleevent";
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

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "scheduleevent.notifEventLinkLabel";
  }

  public UserDetail getSenderUserDetail() {
    return senderUserDetail;
  }
}
