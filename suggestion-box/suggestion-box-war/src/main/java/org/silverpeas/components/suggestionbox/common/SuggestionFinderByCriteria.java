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
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.comparator.AbstractComplexComparator;
import com.stratelia.webactiv.beans.admin.PaginationPage;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteriaProcessor;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepositoryProvider;
import org.silverpeas.contribution.ContributionStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.components.suggestionbox.model.SuggestionCriteria.JOIN_DATA_APPLY;

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
  private List<QUERY_ORDER_BY> logicalOrderBy;

  public SuggestionFinderByCriteria() {
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void endProcessing() {
    SuggestionRepository repository = SuggestionRepositoryProvider.getSuggestionRepository();
    suggestions = repository.findByCriteria(criteria);
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
  public SuggestionCriteriaProcessor processJoinDataApply(
      final List<JOIN_DATA_APPLY> joinDataApplies) {
    if (CollectionUtil.isNotEmpty(joinDataApplies)) {
      if (joinDataApplies.contains(JOIN_DATA_APPLY.COMMENT)) {
        CommentService commentService = SilverpeasServiceProvider.getCommentService();
        List<CommentedPublicationInfo> suggestionInfos = commentService.
            getMostCommentedPublicationsInfo(Suggestion.TYPE, Arrays.asList(new ForeignPK(null,
                criteria.getSuggestionBox().getComponentInstanceId())));

        for (CommentedPublicationInfo info : suggestionInfos) {
          criteria.identifierIsOneOf(info.getPublicationId());
        }
      }
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processOrdering(List<QUERY_ORDER_BY> orderings) {
    if (CollectionUtil.isNotEmpty(orderings)) {
      logicalOrderBy = orderings;
      QUERY_ORDER_BY[] queryOrderBies = new QUERY_ORDER_BY[orderings.size()];
      int i = 0;
      for (QUERY_ORDER_BY queryOrderBy : orderings) {
        if (queryOrderBy.isApplicableOnJpaQuery()) {
          queryOrderBies[i++] = queryOrderBy;
        } else {
          // ordering is requested on a data that is not directly retrieved from jpqlQuery.
          // JPA order by container is unset and logicalOrderBy will be used.
          queryOrderBies = null;
          break;
        }
      }
      if (queryOrderBies != null) {
        // ordering is requested on data that are directly retrieved from jpqlQuery.
        // Logical order by container is unset.
        criteria.orderedBy(queryOrderBies);
        logicalOrderBy = null;
      }
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
    if (logicalOrderBy == null) {
      // pagination can only be done when full JPA ordering is requested or if no ordering is
      // expected. But, for example, if an ordering with logical data is requested,
      // then the pagination is not possible.
      criteria.paginatedBy(pagination);
    }
    return this;
  }

  @Override
  public List<Suggestion> result() {
    if (CollectionUtil.isNotEmpty(logicalOrderBy)) {
      Collections.sort(suggestions, new SuggestionLogicalComparator());
    }
    return suggestions;
  }

  /**
   * This private class handles the logical comparison of suggestion data.
   */
  private class SuggestionLogicalComparator extends AbstractComplexComparator<Suggestion> {

    @Override
    protected ValueBuffer getValuesToCompare(final Suggestion suggestion) {
      ValueBuffer valueBuffer = new ValueBuffer();
      for (QUERY_ORDER_BY queryOrderBy : logicalOrderBy) {
        switch (queryOrderBy) {
          case TITLE_DESC:
          case TITLE_ASC:
            valueBuffer.append(suggestion.getTitle(), queryOrderBy.isAsc());
            break;
          case LAST_UPDATE_DATE_DESC:
          case LAST_UPDATE_DATE_ASC:
            valueBuffer.append(suggestion.getLastUpdateDate(), queryOrderBy.isAsc());
            break;
          case STATUS_ASC:
            valueBuffer.append(suggestion.getValidation().getStatus(), queryOrderBy.isAsc());
            break;
          case VALIDATION_DATE_DESC:
            valueBuffer.append(suggestion.getValidation().getDate(), queryOrderBy.isAsc());
            break;
          case COMMENT_COUNT_DESC:
            valueBuffer.append(suggestion.getCommentCount(), queryOrderBy.isAsc());
            break;
          default:
            throw new UnsupportedOperationException(
                "You must add a new logical data order by management...");
        }
      }
      return valueBuffer;
    }
  }
}
