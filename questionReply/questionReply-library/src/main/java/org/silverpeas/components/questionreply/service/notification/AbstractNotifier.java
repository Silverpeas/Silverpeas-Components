/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.service.notification;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author ehugonnet
 */
abstract class AbstractNotifier extends AbstractTemplateUserNotificationBuilder<Question> {

  private final User sender;

  AbstractNotifier(Question question, User sender) {
    super(question);
    this.sender = sender;
  }

  @Override
  protected void performTemplateData(final String language, final Question question,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("UserDetail", sender);
    template.setAttribute("userName", sender.getDisplayedName());
    template.setAttribute("QuestionDetail", question);
    template.setAttribute("questionTitle", question.getTitle());
    template.setAttribute("questionContent", question.getContent());
    template.setAttribute("silverpeasURL", getResourceURL(question));
  }

  @Override
  protected String getResourceURL(final Question question) {
    return question._getPermalink();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.questionReply.multilang.questionReplyBundle";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "questionReply.notification";
  }

  @Override
  protected String getTitle(final String language) {
    return (super.getTitle(language) + " " + getComponentInstanceTitle(language)).trim();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "questionReply.notifLinkLabel";
  }

  @Override
  protected String getTemplatePath() {
    return "question-reply";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }

  @Override
  protected String getSender() {
    return sender.getId();
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  private String getComponentInstanceTitle(final String language) {
    return SilverpeasComponentInstance.getById(getComponentInstanceId())
        .map(i -> i.getLabel(language))
        .orElse(EMPTY);
  }

  @Override
  protected void performNotificationResource(final String language, final Question resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
  }
}
