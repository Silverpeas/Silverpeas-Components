/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.discussion.notification;

import java.util.Collection;
import org.owasp.encoder.Encode;
import org.silverpeas.components.discussion.service.DiscussionVisioService.Meeting;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

/** Uses each recipient's configured Silverpeas notification channel. */
public class DiscussionVisioNotification extends AbstractTemplateUserNotificationBuilder<Meeting> {
  private final DiscussionVisioEvent invitation;

  public DiscussionVisioNotification(DiscussionVisioEvent invitation) {
    super(invitation.meeting());
    this.invitation = invitation;
  }
  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.components.discussion.multilang.DiscussionBundle";
  }
  @Override
  protected String getBundleSubjectKey() { return "discussion.visioSubject"; }
  @Override
  protected String getTemplateFileName() { return "visioInvitation"; }
  @Override
  protected String getTemplatePath() { return "discussion"; }
  @Override
  protected NotifAction getAction() { return NotifAction.CREATE; }
  @Override
  protected String getComponentInstanceId() { return invitation.instanceId(); }
  @Override
  protected String getSender() { return invitation.sender().getId(); }
  @Override
  protected Collection<String> getUserIdsToNotify() { return invitation.recipients(); }
  @Override
  protected String getContributionAccessLinkLabelBundleKey() { return "discussion.visioJoin"; }
  @Override
  protected String getResourceURL(Meeting meeting) { return meeting.url(); }
  @Override
  protected void performTemplateData(String language, Meeting meeting, SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("author", Encode.forHtml(invitation.sender().getDisplayedName()));
    template.setAttribute("meetingTitle", Encode.forHtml(meeting.title()));
  }
  @Override
  protected void performNotificationResource(String language, Meeting meeting, NotificationResourceData resource) {
    resource.setResourceId(meeting.id());
    resource.setResourceName(meeting.title());
    resource.setResourceType("DiscussionVisio");
  }
}
