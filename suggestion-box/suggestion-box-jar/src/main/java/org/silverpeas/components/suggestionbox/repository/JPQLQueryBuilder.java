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
package org.silverpeas.components.suggestionbox.repository;

import com.stratelia.webactiv.beans.admin.PaginationPage;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteriaProcessor;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.jpa.NamedParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * A dynamic builder of a JPQL query.
 * @author mmoquillon
 */
public class JPQLQueryBuilder implements SuggestionCriteriaProcessor {

  private final StringBuilder query = new StringBuilder("from Suggestion s");
  private final NamedParameters parameters;
  private StringBuilder orderBy = null;
  private boolean done = false;
  private PaginationPage pagination;

  public JPQLQueryBuilder(final NamedParameters parameters) {
    this.parameters = parameters;
  }

  @Override
  public void startProcessing() {
    query.append(" where ");
  }

  @Override
  public void endProcessing() {
    if (orderBy != null) {
      int lastAndIdx = query.lastIndexOf("and ");
      if (lastAndIdx > 0) {
        query.replace(lastAndIdx, query.length(), orderBy.toString());
      } else {
        query.append(orderBy.toString());
      }
    }
    done = true;
  }

  @Override
  public String result() {
    return query.toString();
  }

  @Override
  public SuggestionCriteriaProcessor then() {
    if (!done) {
      query.append(" and ");
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processSuggestionBox(SuggestionBox box) {
    if (!done) {
      query.append("s.suggestionBox = :").append(parameters.add("suggestionBox", box).
          getLastParameterName());
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processCreator(UserDetail creator) {
    if (!done) {
      query.append("s.createdBy = :").append(parameters.add("createdBy", creator.getId()).
          getLastParameterName());
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processStatus(List<ContributionStatus> status) {
    if (!done) {
      query.append("s.validation.status in :").
          append(parameters.add("statuses", status).getLastParameterName());
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processOrdering(List<QUERY_ORDER_BY> orderings) {
    if (!done) {
      orderBy = new StringBuilder("order by ");
      int i = 0;
      for (QUERY_ORDER_BY anOrdering : orderings) {
        if (i > 0) {
          orderBy.append(", ");
        }
        orderBy.append("s.");
        orderBy.append(anOrdering.getPropertyName());
        orderBy.append(" ");
        orderBy.append(anOrdering.isAsc() ? "asc" : "desc");
        i++;
      }
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processIdentifiers(List<String> identifiers) {
    if (!done) {
      List<UuidIdentifier> uuids = new ArrayList<UuidIdentifier>(identifiers.size());
      for (String id : identifiers) {
        uuids.add(new UuidIdentifier().fromString(id));
      }
      query.append("s.id in :").append(parameters.add("ids", uuids).getLastParameterName());
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processPagination(PaginationPage pagination) {
    this.pagination = pagination;
    return this;
  }

  /**
   * Gets the pagination to apply on the results.
   * @return the pagination.
   */
  public PaginationPage getPaginationToApply() {
    return pagination;
  }

}
