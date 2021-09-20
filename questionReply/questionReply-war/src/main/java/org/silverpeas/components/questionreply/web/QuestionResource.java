/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.components.questionreply.web;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.service.QuestionManagerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A REST Web resource representing a given question.
 * It is a web service that provides an access to a question referenced by its URL.
 */
@WebService
@Path(QuestionReplyBaseWebService.PATH + "/{componentId}/questions")
@Authorized
public class QuestionResource extends QuestionReplyBaseWebService {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  /**
   * Gets the JSON representation of the specified existing question.
   * If the question doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the question, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param onQuestionId the unique identifier of the question.
   * @return the response to the HTTP GET request with the JSON representation of the asked question.
   */
  @GET
  @Path("{questionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public QuestionEntity getQuestion(@PathParam("questionId") String onQuestionId) {
    try {
      Question theQuestion = QuestionManagerProvider.getQuestionManager().getQuestion(Long.parseLong(
          onQuestionId));
      URI questionURI = getUri().getRequestUri();
      if (extractVisibleQuestions(Collections.singletonList(theQuestion)).isEmpty()) {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      return asWebEntity(theQuestion, identifiedBy(questionURI));
    } catch (Exception ex) {
      throw encapsulateException(ex);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public QuestionEntity[] getQuestions(@QueryParam("ids") Set<String> ids) {
    try {
      List<Question> questions =
          QuestionManagerProvider.getQuestionManager().getQuestionsByIds(new ArrayList<>(ids));
      return asWebEntities(extractVisibleQuestions(questions));
    } catch (Exception ex) {
      throw encapsulateException(ex);
    }
  }

  @GET
  @Path("all")
  @Produces(MediaType.APPLICATION_JSON)
  public QuestionEntity[] getAllQuestions() {
    try {
      List<Question> questions = QuestionManagerProvider.getQuestionManager().getAllQuestions(
          componentId);
      return asWebEntities(extractVisibleQuestions(questions));
    } catch (Exception ex) {
      throw encapsulateException(ex);
    }
  }

  @GET
  @Path("category/{categoryId}")
  @Produces(MediaType.APPLICATION_JSON)
  public QuestionEntity[] getAllQuestionsByCategory(@PathParam("categoryId") String categoryId) {
    try {
      List<Question> questions = QuestionManagerProvider.getQuestionManager().
          getAllQuestionsByCategory(componentId, categoryId);
      return asWebEntities(extractVisibleQuestions(questions));
    } catch (Exception ex) {
      throw encapsulateException(ex);
    }
  }

  List<Question> extractVisibleQuestions(List<Question> questions) {
    SilverpeasRole profile = getUserProfile();
    List<Question> visibleQuestions;
    if (profile == SilverpeasRole.USER) {
      visibleQuestions = new ArrayList<Question>(questions.size());
      for (Question question : questions) {
        if (question.getPublicReplyNumber() > 0) {
          visibleQuestions.add(question);
        }
      }
    } else {
      visibleQuestions = questions;
    }
    return visibleQuestions;
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }

  /**
   * Converts the specified list of questions into their corresponding web entities.
   * @param questions the questions to convert.
   * @return an array with the corresponding question entities.
   */
  protected QuestionEntity[] asWebEntities(List<Question> questions) {
    QuestionEntity[] entities = new QuestionEntity[questions.size()];
    for (int i = 0; i < questions.size(); i++) {
      Question question = questions.get(i);
      URI questionURI = getUri().getRequestUriBuilder().path(question.getPK().getId()).
          build();
      entities[i] = asWebEntity(question, identifiedBy(questionURI));
    }
    return entities;
  }

  /**
   * Converts the question into its corresponding web entity.
   * @param question the question to convert.
   * @param questionURI the URI of the question.
   * @return the corresponding question entity.
   */
  protected QuestionEntity asWebEntity(final Question question, URI questionURI) {
    QuestionEntity entity = QuestionEntity.fromQuestion(question,
            getUserPreferences().getLanguage()).withURI(questionURI).withUser(getUser(),
            getUserProfile());
    AuthorEntity author = AuthorEntity.fromUser(question.readAuthor(getOrganisationController()));
    author.setAvatar(getHttpServletRequest().getContextPath() + author.getAvatar());
    entity.setCreator(author);
    return entity;
  }
}
