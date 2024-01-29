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

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;

import javax.inject.Named;

/**
 * @author silveryocha
 */
@Named
public class SurveyInstanceManualUserNotification
    extends AbstractComponentInstanceManualUserNotification {

  private static final String QUESTION_CONTAINER_DETAIL_KEY = "QuestionContainerDetailKey";

  @Override
  protected boolean check(final NotificationContext context) {
    final QuestionContainerDetail questionDetail = getSurvey(context);
    context.put(QUESTION_CONTAINER_DETAIL_KEY, questionDetail);
    return questionDetail.canBeAccessedBy(context.getSender());
  }

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final QuestionContainerDetail questionDetail = context.getObject(QUESTION_CONTAINER_DETAIL_KEY);
    return new SurveyUserAlertNotification(questionDetail, context.getSender()).build();
  }

  private QuestionContainerDetail getSurvey(final NotificationContext context) {
    try {
      final String componentId = context.getComponentId();
      final String surveyId = context.getContributionId();
      final QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, "", componentId);
      return QuestionContainerService.get().getQuestionContainer(qcPK, context.getSender().getId());
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}
