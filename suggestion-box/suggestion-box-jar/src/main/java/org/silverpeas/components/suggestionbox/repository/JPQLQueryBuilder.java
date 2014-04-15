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
import org.silverpeas.persistence.repository.PaginationCriterion;
import org.silverpeas.persistence.repository.QueryCriteria;
import org.silverpeas.persistence.repository.SimpleQueryCriteria;
import org.silverpeas.persistence.repository.jpa.NamedParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * A dynamic builder of a JPQL query.
 * @author mmoquillon
 */
public class JPQLQueryBuilder implements SuggestionCriteriaProcessor {

  private StringBuilder orderBy = null;
  private boolean done = false;
  private final SimpleQueryCriteria jpqlCriteria;
  private String conjonction;

  public JPQLQueryBuilder(final NamedParameters parameters) {
    this.jpqlCriteria = new SimpleQueryCriteria(parameters);
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void endProcessing() {
    if (orderBy != null && orderBy.length() > 0) {
      jpqlCriteria.clause().add(orderBy.toString());
    }
    done = true;
  }

  @Override
  public QueryCriteria result() {
    return this.jpqlCriteria;
  }

  @Override
  public SuggestionCriteriaProcessor then() {
    if (!done) {
      conjonction = "and";
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processSuggestionBox(SuggestionBox box) {
    if (!done) {
      jpqlCriteria.clause().add(conjonction).add("suggestionBox = :suggestionBox").parameters()
          .add("suggestionBox", box);
      conjonction = null;
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processCreator(UserDetail creator) {
    if (!done) {
      jpqlCriteria.clause().add(conjonction).add("createdBy = :createdBy").parameters()
          .add("createdBy", creator.
              getId());
      conjonction = null;
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processStatus(List<ContributionStatus> status) {
    if (!done) {
      jpqlCriteria.clause().add(conjonction).add("validation.status in :statuses").parameters()
          .add("statuses", status);
      conjonction = null;
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
        orderBy.append(anOrdering.getPropertyName());
        orderBy.append(" ");
        orderBy.append(anOrdering.isAsc() ? "asc" : "desc");
        i++;
      }
      conjonction = null;
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
      jpqlCriteria.clause().add(conjonction).add("id in :ids").parameters().add("ids", uuids);
      conjonction = null;
    }
    return this;
  }

  @Override
  public SuggestionCriteriaProcessor processPagination(PaginationPage pagination) {
    jpqlCriteria.withPagination(new PaginationCriterion(pagination.getPageNumber(), pagination.
        getPageSize()));
    conjonction = null;
    return this;
  }

}
