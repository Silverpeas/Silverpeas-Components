/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.web;

import org.silverpeas.components.suggestionbox.common.SuggestionBoxWebManager;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.components.suggestionbox.model.SuggestionCriteria.JOIN_DATA_APPLY;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs.BOX_BASE_URI;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs
    .BOX_SUGGESTION_URI_PART;

/**
 * A REST Web resource giving suggestion data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(BOX_BASE_URI + "/{componentInstanceId}/{suggestionBoxId}")
@Authorized
public class SuggestionBoxResource extends AbstractSuggestionBoxResource {

  @Inject
  private CommentService commentService;

  @Inject
  private SuggestionBoxWebManager suggestionBoxWebManager;

  /**
   * Gets the JSON representation of an suggestion.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param suggestionId the identifier of the suggestion
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * suggestion.
   * @see RESTWebService.WebProcess#execute()
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/{suggestionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public SuggestionEntity getSuggestion(@PathParam("suggestionId") final String suggestionId) {
    return process(() -> {
      final Suggestion suggestion = getSuggestionBox().getSuggestions().get(suggestionId);
      return suggestionBoxWebManager.asWebEntity(suggestion);
    }).execute();
  }

  /**
   * Deletes the suggestion identified by the specified identifier.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param suggestionId the identifier of the suggestion.
   */
  @DELETE
  @Path(BOX_SUGGESTION_URI_PART + "/{suggestionId}")
  public void deleteSuggestion(@PathParam("suggestionId") final String suggestionId) {
    process(() -> {
      final Suggestion suggestion = getSuggestionBox().getSuggestions().get(suggestionId);
      suggestionBoxWebManager
          .deleteSuggestion(getSuggestionBox(), suggestion, getUser());
      return null;
    }).lowestAccessRole(SilverpeasRole.writer).execute();
  }

  /**
   * Publishes the suggestion identified by the specified identifier.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param suggestionId the identifier of the suggestion.
   * @return the response to the HTTP PUT request with the JSON representation of the published
   * suggestion.
   */
  @PUT
  @Path(BOX_SUGGESTION_URI_PART + "/{suggestionId}/publish")
  @Produces(MediaType.APPLICATION_JSON)
  public SuggestionEntity publishSuggestion(@PathParam("suggestionId") final String suggestionId) {
    return process(() -> {
      final Suggestion suggestion = getSuggestionBox().getSuggestions().get(suggestionId);
      return suggestionBoxWebManager
          .publishSuggestion(getSuggestionBox(), suggestion, getUser());
    }).lowestAccessRole(SilverpeasRole.writer).execute();
  }

  /**
   * Gets the JSON representation of a list of suggestion that are in draft for the user
   * behind the service call.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * list of suggestions.
   * @see SuggestionBoxWebManager#getSuggestionsInDraftFor(SuggestionBox, User)
   * @see WebProcess#execute()
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/inDraft")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SuggestionEntity> getSuggestionsInDraft() {
    return process(() -> suggestionBoxWebManager
        .getSuggestionsInDraftFor(getSuggestionBox(), getUser()))
        .lowestAccessRole(SilverpeasRole.writer).execute();
  }

  /**
   * Gets the JSON representation of a list of suggestion that are out of draft for the user
   * behind the service call.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * list of suggestions.
   * @see SuggestionBoxWebManager#getSuggestionsInDraftFor(SuggestionBox, User)
   * @see WebProcess#execute()
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/outOfDraft")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SuggestionEntity> getSuggestionsOutOfDraft() {
    return process(() -> suggestionBoxWebManager
        .getSuggestionsOutOfDraftFor(getSuggestionBox(), getUser()))
        .lowestAccessRole(SilverpeasRole.writer).execute();
  }

  /**
   * Gets the JSON representation of a list of suggestion that are pending validation.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * list of suggestions.
   * @see SuggestionBoxWebManager#getSuggestionsInPendingValidation(SuggestionBox)
   * @see WebProcess#execute()
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/pendingValidation")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SuggestionEntity> getSuggestionsInPendingValidation() {
    return process(
        () -> suggestionBoxWebManager.getSuggestionsInPendingValidation(getSuggestionBox()))
        .lowestAccessRole(SilverpeasRole.publisher).execute();
  }

  /**
   * Gets the JSON representation of a list of suggestions that are published, bu default sorted by
   * date of validation (from the newer to the older).
   * @param authorId if this parameter is set, then the suggestions to get will be those proposed by
   * the author with the specified user unique identifier.
   * @param page if this parameter is set, then the pagination is activated and only the published
   * suggestions matching the specified pagination criterion are sent back. The parameter is
   * semicolon-separated criterion: the first part is the page number and the second part is the
   * count of suggestions to sent back.
   * @param property the property by which the asked published suggestions should be sorted. If no
   * such property exists for the suggestions, then no sorting is performed.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * list of suggestions.
   * @see SuggestionBoxWebManager#getPublishedSuggestions(SuggestionBox)
   * @see WebProcess#execute()
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/published")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SuggestionEntity> getPublishedSuggestions(
      @QueryParam("author") final String authorId,
      @QueryParam("page") final String page,
      @QueryParam("sortby") final String property) {
    return process(() -> {
      PaginationPage pagination = fromPage(page);
      User author = (StringUtil.isDefined(authorId) ? User.getById(authorId) : null);
      if (pagination != null || StringUtil.isDefined(property)) {
        JOIN_DATA_APPLY commentJoinData = null;
        QUERY_ORDER_BY orderBy = QUERY_ORDER_BY.fromPropertyName(property);
        if (QUERY_ORDER_BY.COMMENT_COUNT_DESC.equals(orderBy)) {
          commentJoinData = JOIN_DATA_APPLY.COMMENT;
        }
        List<SuggestionEntity> suggestions = suggestionBoxWebManager.
            getSuggestionsByCriteria(SuggestionCriteria.from(getSuggestionBox())
                    .createdBy(author)
                    .statusIsOneOf(ContributionStatus.VALIDATED)
                    .paginatedBy(pagination)
                    .applyJoinOnData(commentJoinData)
                    .orderedBy(QUERY_ORDER_BY.fromPropertyName(property)));
        if (suggestions instanceof PaginationList) {
          String maxlength = String.valueOf(((PaginationList) suggestions).originalListSize());
          getHttpServletResponse().setHeader(RESPONSE_HEADER_ARRAYSIZE, maxlength);
        }
        return suggestions;
      } else if (author != null) {
        return suggestionBoxWebManager
            .getPublishedSuggestionsFor(getSuggestionBox(), author);
      }
      return suggestionBoxWebManager.getPublishedSuggestions(getSuggestionBox());
    }).execute();
  }


  /**
   * Gets the JSON representation of a list of the last comments that were posted on the
   * suggestions
   * of the current suggestion box. The comments are sorted in descent order, that is to say from
   * the newer to the older one.
   * @param count the number of comments to return. If this parameter isn't set, then only the
   * first
   * 5 last comments are sent back.
   * @return a collection of comments on the suggestions, ready to be serialized in JSON.
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/lastComments")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SuggestionCommentEntity> getLastComments(@QueryParam("count") final int count) {
    SuggestionBox suggestionBox = getSuggestionBox();
    List<Comment> comments = commentService
        .getLastComments(suggestionBox.getComponentInstanceId(), (count <= 0 ? 5 : count));
    List<SuggestionCommentEntity> commentEntities =
        new ArrayList<SuggestionCommentEntity>(comments.size());
    for (Comment comment : comments) {
      Suggestion suggestion = suggestionBox.getSuggestions().get(comment.getForeignKey().getId());
      commentEntities.add(SuggestionCommentEntity.fromComment(comment).onSuggestion(suggestion));
    }
    return commentEntities;
  }
}
