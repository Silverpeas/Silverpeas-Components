/*
 * Copyright (C) 2000 - 2012 Silverpeas
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p/>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection withWriter Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along withWriter this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.control.QuestionManagerFactory;
import com.silverpeas.questionReply.model.Reply;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;

/**
 *
 * @author emmanuel.hugonnet@silverpeas.org
 */
/**
 * A REST Web resource representing a given reply to a question. It is a web service that provides
 * an access to a reply referenced by its URL.
 */
@Service
@RequestScoped
@Path("questionreply/{componentId}/replies")
@Authorized
public class ReplyResource extends QuestionRelyBaseWebService {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
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
    try {
      long questionId = Long.parseLong(onQuestionId);
      List<Reply> replies = QuestionManagerFactory.getQuestionManager().getAllReplies(questionId,
              componentId);
      return asWebEntities(extractVisibleReplies(questionId, replies), getUserProfile());
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("public/question/{questionId}")
  public ReplyEntity[] getPublicRepliesForQuestion(@PathParam("questionId") String onQuestionId) {
    try {
      List<Reply> replies = QuestionManagerFactory.getQuestionManager().getQuestionPublicReplies(
              Long.parseLong(onQuestionId), componentId);
      return asWebEntities(replies, getUserProfile());
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
   * @param profile the profile of the user.
   * @return an array with the corresponding reply entities.
   */
  protected ReplyEntity[] asWebEntities(List<Reply> replies, SilverpeasRole profile) {
    ReplyEntity[] entities = new ReplyEntity[replies.size()];
    for (int i = 0; i < replies.size(); i++) {
      Reply reply = replies.get(i);
      URI commentURI = getUriInfo().getRequestUriBuilder().path(reply.getPK().getId()).build();
      entities[i] = asWebEntity(reply, identifiedBy(commentURI), profile);
    }
    return entities;
  }

  /**
   * Converts the reply into its corresponding web entity.
   * @param reply the reply to convert.
   * @param replyURI the URI of the reply.
   * @param profile the profile of the user.
   * @return the corresponding reply entity.
   */
  protected ReplyEntity asWebEntity(final Reply reply, URI replyURI, SilverpeasRole profile) {
    ReplyEntity entity = ReplyEntity.fromReply(reply, getUserPreferences().getLanguage()).withURI(
            replyURI).withProfile(profile);
    Collection<AttachmentDetail> attachments = AttachmentController.searchAttachmentByPKAndContext(reply.
            getPK(), "Images");
    entity.withAttachments(attachments);
    AuthorEntity author = AuthorEntity.fromUser(reply.readAuthor(getOrganizationController()));
    author.setAvatar(getHttpServletRequest().getContextPath() + author.getAvatar());
    return entity;
  }

  /**
   * Private replies should be visible to writers or publishers that have asked the question.
   * @param questionAuthor
   * @param reply
   * @param role
   * @param userId
   * @return
   */
  boolean isReplyVisible(String questionAuthor, Reply reply, SilverpeasRole role,
          String userId) {
    boolean isPrivate = reply.getPublicReply() <= 0;
    if (isPrivate) {
      boolean isAuthor = questionAuthor.equals(userId);
      if (SilverpeasRole.user == role || (SilverpeasRole.publisher == role && !isAuthor)) {
        return false;
      }
    }
    return true;
  }

  List<Reply> extractVisibleReplies(long questionId, List<Reply> replies) throws
          QuestionReplyException {
    List<Reply> visibleReplies = new ArrayList<Reply>(replies.size());
    String authorId = QuestionManagerFactory.getQuestionManager().getQuestion(questionId).
            getCreatorId();
    SilverpeasRole profile = getUserProfile();
    String userid = getUserDetail().getId();
    for (Reply reply : replies) {
      if (isReplyVisible(authorId, reply, profile, userid)) {
        visibleReplies.add(reply);
      }
    }
    return visibleReplies;
  }
}
