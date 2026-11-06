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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.web;

import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;

/**
 *
 */
public abstract class QuestionReplyBaseWebService extends RESTWebService {

  static final String PATH = "questionreply";

  @Inject
  private QuestionManager questionManager;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  QuestionManager getQuestionManager() {
    return questionManager;
  }

  /**
   * Gets the specified question of the FAQ targeted by the request. The user is authorized on this
   * FAQ only, so a question of another FAQ is never served.
   * @param questionId the unique identifier of the question.
   * @return the question.
   * @throws WebApplicationException with a 404 HTTP code if the FAQ has no such question.
   * @throws QuestionReplyException if an error occurs while fetching the question.
   */
  Question getQuestionInComponent(String questionId) throws QuestionReplyException {
    Question question = questionManager.getQuestion(Long.parseLong(questionId));
    if (question == null || !getComponentId().equals(question.getInstanceId())) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return question;
  }

  SilverpeasRole getUserProfile() {
    String[] roles =
        getOrganisationController().getUserProfiles(getUser().getId(), getComponentId());
    SilverpeasRole profile = USER;
    for (String currentRole : roles) {
      SilverpeasRole role = SilverpeasRole.fromString(currentRole);
      switch (role) {
        case ADMIN:
          return ADMIN;
        case WRITER:
          profile = WRITER;
          break;
        case PUBLISHER:
          if (profile != WRITER) {
            profile = PUBLISHER;
          }
          break;
        default:
          if (profile != PUBLISHER && profile != WRITER) {
            profile = role;
          }
          break;
      }
    }
    return profile;
  }

  WebApplicationException encapsulateException(Exception ex) {
    if (ex instanceof WebApplicationException) {
      return (WebApplicationException) ex;
    }
    return new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
  }
}
