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

import com.stratelia.webactiv.beans.admin.PaginationPage;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteriaProcessor;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.contribution.ContributionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * It applies the criteria on suggestions to a given list of web suggestion entities.
 * @author mmoquillon
 */
public class SuggestionCriteriaApplier implements SuggestionCriteriaProcessor {

  private List<SuggestionEntity> suggestions;
  private UserDetail creator;
  private List<ContributionStatus> status;
  private List<QUERY_ORDER_BY> orderings;
  private List<String> identifiers;
  private boolean process = false;
  private PaginationPage pagination;

  public SuggestionCriteriaApplier(final List<SuggestionEntity> suggestions) {
    this.suggestions = new ArrayList<SuggestionEntity>(suggestions);
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void endProcessing() {
    if (process) {
      List<SuggestionEntity> result = new ArrayList<SuggestionEntity>(suggestions.size());
      for (SuggestionEntity suggestionEntity : suggestions) {
        if (creator != null && !suggestionEntity.getAuthor().equals(creator.getDisplayedName())) {
          continue;
        }
        if (status != null && !status.contains(suggestionEntity.getValidation().getStatus())) {
          continue;
        }
        if (identifiers != null && !identifiers.contains(suggestionEntity.getId())) {
          continue;
        }
        result.add(suggestionEntity);
      }
      suggestions = result;
      // TODO : the ordering
    }
    if (pagination != null) {
      int size = suggestions.size();
      int start = (pagination.getPageNumber() - 1) * pagination.getPageSize();
      int end = start + pagination.getPageSize();
      suggestions = new PaginatedList<SuggestionEntity>(suggestions.subList(start,
          (end < size ? end : size)), size);
    }
  }

  @Override
  public SuggestionCriteriaProcessor then() {
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processSuggestionBox(SuggestionBox box
  ) {
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processCreator(UserDetail creator
  ) {
    this.creator = creator;
    process(creator);
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processStatus(List<ContributionStatus> status
  ) {
    this.status = status;
    process(status);
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processOrdering(List<QUERY_ORDER_BY> orderings
  ) {
    this.orderings = orderings;
    process(orderings);
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processIdentifiers(List<String> identifiers
  ) {
    this.identifiers = identifiers;
    process(identifiers);
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processPagination(PaginationPage pagination
  ) {
    this.pagination = pagination;
    return this;
  }

  @Override
  public List<SuggestionEntity> result() {
    return suggestions;
  }


  private void process(Object o) {
    if (o != null) {
      process = true;
    }
  }

}
