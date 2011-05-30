/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.silverpeas.questionReply.control.QuestionManagerFactory;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.rest.RESTWebService;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * A REST Web resource representing a given comment.
 * It is a web service that provides an access to a comment referenced by its URL.
 */
@Service
@Scope("request")
@Path("comments/{componentId}/{questionId}")
public class QuestionResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;
  @PathParam("questionId")
  private String questionId;

  @Override
  protected String getComponentId() {
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
    checkUserPriviledges();
    try {
      Question theQuestion = QuestionManagerFactory.getQuestionManager().getQuestion(Long.parseLong(
          onQuestionId));
      URI questionURI = getUriInfo().getRequestUri();
      return asWebEntity(theQuestion, identifiedBy(questionURI));
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
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
      URI commentURI = getUriInfo().getRequestUriBuilder().path(question.getPK().getId()).
          build();
      entities[i] = asWebEntity(question, identifiedBy(commentURI));
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
    QuestionEntity entity = QuestionEntity.fromQuestion(question).withURI(questionURI);
    AuthorEntity author = AuthorEntity.fromUser(question.readAuthor());
    author.setAvatar(getHttpServletContext().getContextPath() + author.getAvatar());
    return entity;
  }

}
