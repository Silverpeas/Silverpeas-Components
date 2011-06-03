/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.questionReply.control.QuestionManagerFactory;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.rest.RESTWebService;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import java.net.URI;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 *
 * @author emmanuel.hugonnet@silverpeas.org
 */
/**
 * A REST Web resource representing a given reply to a question.
 * It is a web service that provides an access to a reply referenced by its URL.
 */
@Service
@Scope("request")
@Path("questionreply/{componentId}/replies")
public class ReplyResource extends RESTWebService {

  @Inject
  private OrganizationController controller;
  @PathParam("componentId")
  private String componentId;

  @Override
  protected String getComponentId() {
    return this.componentId;
  }

  /**
   * Gets the JSON representation of the specified existing question.
   * If the reply doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the question, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param onQuestionId the unique identifier of the question.
   * @return the response to the HTTP GET request with the JSON representation of the asked question replies.
   */
  @GET
  @Path("question/{questionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ReplyEntity[] getAllRepliesForQuestion(@PathParam("questionId") String onQuestionId) {
    checkUserPriviledges();
    try {
      List<Reply> replies = QuestionManagerFactory.getQuestionManager().getAllReplies(Long.parseLong(
          onQuestionId));
      return asWebEntities(replies);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("question/public/{questionId}")
  public ReplyEntity[] getPublicRepliesForQuestion(@PathParam("questionId") String onQuestionId) {
    checkUserPriviledges();
    try {
      List<Reply> replies = QuestionManagerFactory.getQuestionManager().getQuestionPublicReplies(Long.
          parseLong(onQuestionId));
      return asWebEntities(replies);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }

  /**
   * Converts the specified list of replies into their corresponding web entities.
   * @param replies the replies to convert.
   * @return an array with the corresponding reply entities.
   */
  protected ReplyEntity[] asWebEntities(List<Reply> replies) {
    ReplyEntity[] entities = new ReplyEntity[replies.size()];
    for (int i = 0; i < replies.size(); i++) {
      Reply reply = replies.get(i);
      URI commentURI = getUriInfo().getRequestUriBuilder().path(reply.getPK().getId()).build();
      entities[i] = asWebEntity(reply, identifiedBy(commentURI));
    }
    return entities;
  }

  /**
   * Converts the reply into its corresponding web entity.
   * @param reply the reply to convert.
   * @param replyURI the URI of the reply.
   * @return the corresponding reply entity.
   */
  protected ReplyEntity asWebEntity(final Reply reply, URI replyURI) {
    ReplyEntity entity = ReplyEntity.fromReply(reply).withURI(replyURI);
    AuthorEntity author = AuthorEntity.fromUser(reply.readAuthor(controller));
    author.setAvatar(getHttpServletContext().getContextPath() + author.getAvatar());
    return entity;
  }
}
