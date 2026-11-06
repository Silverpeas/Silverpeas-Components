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

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A REST Web resource representing a given reply to a question. It is a web service that provides
 * access to a reply referenced by its URL.
 *
 * @author emmanuel.hugonnet@silverpeas.org
 */
@WebService
@Path(QuestionReplyBaseWebService.PATH + "/{componentId}/replies")
@Authorized
public class ReplyResource extends QuestionReplyBaseWebService {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  /**
   * Gets the JSON representation of the replies to the specified question. If the question doesn't
   * exist in the FAQ, a 404 HTTP code is returned. If the user isn't authentified, a 401 HTTP code
   * is returned. If the user isn't authorized to access the question, a 403 is returned. If a
   * problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param onQuestionId the unique identifier of the question.
   * @return the response to the HTTP GET request with the JSON representation of the asked question
   * replies.
   */
  @GET
  @Path("question/{questionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ReplyEntity[] getAllRepliesForQuestion(@PathParam("questionId") String onQuestionId) {
    try {
      Question question = getQuestionInComponent(onQuestionId);
      List<Reply> replies =
          getQuestionManager().getAllReplies(Long.parseLong(onQuestionId), componentId);
      return asWebEntities(extractVisibleReplies(question, replies), getUserProfile());
    } catch (Exception ex) {
      throw encapsulateException(ex);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("public/question/{questionId}")
  public ReplyEntity[] getPublicRepliesForQuestion(@PathParam("questionId") String onQuestionId) {
    try {
      getQuestionInComponent(onQuestionId);
      List<Reply> replies = getQuestionManager().getQuestionPublicReplies(
          Long.parseLong(onQuestionId), componentId);
      return asWebEntities(replies, getUserProfile());
    } catch (Exception ex) {
      throw encapsulateException(ex);
    }
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }

  /**
   * Converts the specified list of replies into their corresponding web entities.
   *
   * @param replies the replies to convert.
   * @param profile the profile of the user.
   * @return an array with the corresponding reply entities.
   */
  protected ReplyEntity[] asWebEntities(List<Reply> replies, SilverpeasRole profile) {
    ReplyEntity[] entities = new ReplyEntity[replies.size()];
    for (int i = 0; i < replies.size(); i++) {
      Reply reply = replies.get(i);
      URI commentURI = getUri().getRequestUriBuilder().path(reply.getPK().getId()).build();
      entities[i] = asWebEntity(reply, identifiedBy(commentURI), profile);
    }
    return entities;
  }

  /**
   * Converts the reply into its corresponding web entity.
   *
   * @param reply the reply to convert.
   * @param replyURI the URI of the reply.
   * @param profile the profile of the user.
   * @return the corresponding reply entity.
   */
  protected ReplyEntity asWebEntity(final Reply reply, URI replyURI, SilverpeasRole profile) {
    ReplyEntity entity = ReplyEntity.fromReply(reply, getUserPreferences().getLanguage()).withURI(
        replyURI).withProfile(profile);
    Collection<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(reply.getPK().toResourceReference(), entity.getLanguage());
    entity.withAttachments(attachments);
    AuthorEntity author = AuthorEntity.fromUser(reply.readAuthor());
    author.setAvatar(getHttpServletRequest().getContextPath() + author.getAvatar());
    return entity;
  }

  /**
   * Private replies should be visible to writers or publishers that have asked the question.
   *
   * @param questionAuthor the unique identifier of the question's author
   * @param reply the reply to the question
   * @param role the role the requester plays
   * @param userId the unique identifier of the requester
   * @return true if the reply is visible to the requester (the current user).
   */
  boolean isReplyVisible(String questionAuthor, Reply reply, SilverpeasRole role,
      String userId) {
    boolean isPrivate = reply.getPublicReply() <= 0;
    if (isPrivate) {
      boolean isAuthor = questionAuthor.equals(userId);
      return SilverpeasRole.USER != role && (SilverpeasRole.PUBLISHER != role || isAuthor);
    }
    return true;
  }

  List<Reply> extractVisibleReplies(Question question, List<Reply> replies) {
    String authorId = question.getCreatorId();
    SilverpeasRole profile = getUserProfile();
    String userid = getUser().getId();
    return replies.stream()
        .filter(reply -> isReplyVisible(authorId, reply, profile, userid))
        .collect(Collectors.toCollection(() -> new ArrayList<>(replies.size())));
  }
}
