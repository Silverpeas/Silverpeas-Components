/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.common;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.beans.admin.PaginationPage;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxServiceFactory;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteriaProcessor;
import org.silverpeas.contribution.ContributionStatus;

import java.util.Arrays;
import java.util.List;

/**
 * A finder of suggestions in the given suggestion box by applying a criteria on the suggestion
 * in the business service layer.
 * <p>
 * It applies the criteria on the suggestions by using the several business services on which a
 * suggestion is relied on. For example, a suggestion used the WYSIWYG service for its rich content
 * as well as the comment service for the user comments on it.
 * @author mmoquillon
 */
public class SuggestionFinderByCriteria implements SuggestionCriteriaProcessor {

  private List<Suggestion> suggestions;
  private SuggestionCriteria criteria;

  public SuggestionFinderByCriteria() {
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void endProcessing() {
    SuggestionBoxService suggestionBoxService = SuggestionBoxServiceFactory.getFactory().
        getSuggestionBoxService();
    suggestions = suggestionBoxService.findSuggestionsByCriteria(criteria);
  }

  @Override
  public SuggestionCriteriaProcessor then() {
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processSuggestionBox(SuggestionBox box) {
    criteria = SuggestionCriteria.from(box);
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processCreator(UserDetail creator) {
    criteria.createdBy(creator);
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processStatus(List<ContributionStatus> status) {
    criteria.statusIsOneOf(status.toArray(new ContributionStatus[status.size()]));
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processOrdering(List<QUERY_ORDER_BY> orderings) {
    if (orderings != null && !orderings.isEmpty()) {
      if (orderings.contains(QUERY_ORDER_BY.COMMENT_COUNT_DESC)) {
        orderings.remove(QUERY_ORDER_BY.COMMENT_COUNT_DESC);
        CommentService commentService = SilverpeasServiceProvider.getCommentService();
        List<CommentedPublicationInfo> suggestionInfos = commentService.
            getMostCommentedPublicationsInfo(Suggestion.TYPE, Arrays.asList(new ForeignPK(null,
                        criteria.getSuggestionBox().getComponentInstanceId())));

        for (CommentedPublicationInfo info : suggestionInfos) {
          criteria.identifierIsOneOf(info.getPublicationId());
        }
      }
      criteria.orderedBy(orderings.toArray(new QUERY_ORDER_BY[orderings.size()]));
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processIdentifiers(List<String> identifiers) {
    criteria.identifierIsOneOf(identifiers.toArray(new String[identifiers.size()]));
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processPagination(PaginationPage pagination) {
    criteria.paginatedBy(pagination);
    return this;
  }

  @Override
  public List<Suggestion> result() {
    return suggestions;
  }

}
