/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.questionReply.control.QuestionManagerFactory;
import com.silverpeas.questionReply.model.Question;
import com.stratelia.webactiv.SilverpeasRole;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;

/**
 * A REST Web resource representing a given question.
 * It is a web service that provides an access to a question referenced by its URL.
 */
@Service
@RequestScoped
@Path("questionreply/{componentId}/questions")
@Authorized
public class QuestionResource extends QuestionRelyBaseWebService {

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
      Question theQuestion = QuestionManagerFactory.getQuestionManager().getQuestion(Long.parseLong(
          onQuestionId));
      URI questionURI = getUriInfo().getRequestUri();
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
  public QuestionEntity[] getAllQuestions(@PathParam("questionId") String onQuestionId) {
    try {
      List<Question> questions = QuestionManagerFactory.getQuestionManager().getAllQuestions(
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
      List<Question> questions = QuestionManagerFactory.getQuestionManager().
          getAllQuestionsByCategory(componentId, categoryId);
      return asWebEntities(extractVisibleQuestions(questions));
    } catch (Exception ex) {
      throw encapsulateException(ex);
    }
  }

  List<Question> extractVisibleQuestions(List<Question> questions) {
    SilverpeasRole profile = getUserProfile();
    List<Question> visibleQuestions;
    if (profile == SilverpeasRole.user) {
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
      URI questionURI = getUriInfo().getRequestUriBuilder().path(question.getPK().getId()).
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
            getUserPreferences().getLanguage()).withURI(questionURI).withUser(getUserDetail(), 
            getUserProfile());
    AuthorEntity author = AuthorEntity.fromUser(question.readAuthor(getOrganisationController()));
    author.setAvatar(getHttpServletRequest().getContextPath() + author.getAvatar());
    entity.setCreator(author);
    return entity;
  }
}
