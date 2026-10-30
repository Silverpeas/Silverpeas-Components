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
import java.util.Set;
import org.owasp.encoder.Encode;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

/** A native Silverpeas notification for newly added, authorized mentions. */
public class DiscussionMentionNotification extends
    AbstractTemplateUserNotificationBuilder<DiscussionMessage> {
  private final String instanceId;
  private final User sender;
  private final Set<String> recipients;

  public DiscussionMentionNotification(String instanceId, DiscussionMessage message,
      User sender, Set<String> recipients) {
    super(message);
    this.instanceId = instanceId;
    this.sender = sender;
    this.recipients = Set.copyOf(recipients);
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.components.discussion.multilang.DiscussionBundle";
  }
  @Override
  protected String getBundleSubjectKey() { return "discussion.mentionSubject"; }
  @Override
  protected String getTemplateFileName() { return "mentionNotification"; }
  @Override
  protected String getTemplatePath() { return "discussion"; }
  @Override
  protected NotifAction getAction() { return NotifAction.UPDATE; }
  @Override
  protected String getComponentInstanceId() { return instanceId; }
  @Override
  protected String getSender() { return sender.getId(); }
  @Override
  protected Collection<String> getUserIdsToNotify() { return recipients; }
  @Override
  protected String getContributionAccessLinkLabelBundleKey() { return "discussion.viewMessage"; }
  @Override
  protected String getResourceURL(DiscussionMessage message) {
    return URLUtil.getApplicationURL() + "/Rdiscussion/" + instanceId + "/messages/" + message.getId()
        + "#message-" + message.getId();
  }
  @Override
  protected void performTemplateData(String language, DiscussionMessage message,
      SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("author", Encode.forHtml(sender.getDisplayedName()));
    template.setAttribute("body", Encode.forHtml(message.getBody()));
  }
  @Override
  protected void performNotificationResource(String language, DiscussionMessage message,
      NotificationResourceData resource) {
    resource.setResourceId(message.getId());
    resource.setResourceName(getTitle(language));
    resource.setResourceType("DiscussionMessage");
  }
}
