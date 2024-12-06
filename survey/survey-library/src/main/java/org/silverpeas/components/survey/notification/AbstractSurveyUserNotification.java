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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.survey.notification;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.kernel.util.StringUtil;

/**
 * The centralization of the construction of the survey notifications
 * @author silveryocha
 */
public abstract class AbstractSurveyUserNotification
    extends AbstractTemplateUserNotificationBuilder<QuestionContainerDetail> {

  private final User sender;

  AbstractSurveyUserNotification(final QuestionContainerDetail surveyDetail, final User sender) {
    super(surveyDetail);
    this.sender = sender;
  }

  @Override
  protected void performTemplateData(final String language, final QuestionContainerDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("UserDetail", this.sender);
    template.setAttribute("userName",
        this.sender != null ? this.sender.getDisplayedName() : "");
    template.setAttribute("SurveyDetail", resource);
    template.setAttribute("surveyName", Encode.forHtml(resource.getHeader().getName()));
    String surveyDesc = resource.getHeader().getDescription();
    if (StringUtil.isDefined(surveyDesc)) {
      template.setAttribute("surveyDesc", Encode.forHtml(surveyDesc));
    }
    final String pathToSurvey = QuestionContainerService.get().getHTMLQuestionPath(resource);
    template.setAttribute("htmlPath", pathToSurvey);
  }

  @Override
  protected void performNotificationResource(final String language,
      final QuestionContainerDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getHeader().getName());
  }

  @Override
  protected String getTemplatePath() {
    return "survey";
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
    return this.sender.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.survey.multilang.surveyBundle";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "survey.notifSurveyLinkLabel";
  }
}
