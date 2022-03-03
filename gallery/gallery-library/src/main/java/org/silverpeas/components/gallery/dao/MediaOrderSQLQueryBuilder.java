/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.dao;

import org.silverpeas.components.gallery.model.MediaOrderCriteriaProcessor;
import org.silverpeas.components.gallery.model.Order;
import org.apache.commons.lang3.time.DateUtils;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A dynamic builder of a SQL query.
 */
public class MediaOrderSQLQueryBuilder implements MediaOrderCriteriaProcessor {

  private boolean done = false;
  private final StringBuilder sqlQuery = new StringBuilder();
  private final StringBuilder from = new StringBuilder();
  private final StringBuilder where = new StringBuilder();
  private final List<Object> parameters = new ArrayList<>();
  private String conjunction = "";

  @Override
  public void startProcessing() {
    sqlQuery
        .append("O.orderId, O.userId, O.instanceId, O.createDate, O.processDate, O.processUser ");
    from.append("from SC_Gallery_Order O ");
  }

  @Override
  public void endProcessing() {
    sqlQuery.append(from.toString());
    if (where.length() > 0) {
      sqlQuery.append(" where ").append(where.toString());
    }
    done = true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public JdbcSqlQuery result() {
    return JdbcSqlQuery.createSelect(sqlQuery.toString(), parameters);
  }

  @Override
  public MediaOrderCriteriaProcessor then() {
    if (!done) {
      conjunction = " and ";
    }
    return this;
  }

  @Override
  public MediaOrderCriteriaProcessor processComponentInstance(String componentInstanceId) {
    if (!done) {
      where(conjunction).append("O.instanceId = ?");
      parameters.add(componentInstanceId);
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaOrderCriteriaProcessor processOrderer(String ordererId) {
    if (!done) {
      where(conjunction).append("O.userId = ?");
      parameters.add(ordererId);
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaOrderCriteriaProcessor processNbDaysAfterThatDeleteAnOrder(final Date referenceDate,
      final int nbDaysAfterThatDeleteAnOrder) {
    if (!done) {
      where(conjunction).append("O.createDate < ?");
      parameters.add(DateUtils.addDays(referenceDate, -nbDaysAfterThatDeleteAnOrder));
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaOrderCriteriaProcessor processIdentifiers(List<String> identifiers) {
    if (!done) {
      StringBuilder params = new StringBuilder();
      for (String identifier : identifiers) {
        if (params.length() > 0) {
          params.append(",");
        }
        params.append("?");
        parameters.add(identifier);
      }
      where(conjunction).append("O.orderId in (").append(params.toString()).append(")");
      conjunction = "";
    }
    return this;
  }

  @Override
  public List<Order> orderingResult(final List<Order> orders) {
    return orders;
  }

  /**
   * Centralization to perform the where clause.
   * @param conjunction the conjunction.
   * @return the builder completed with conjunction.
   */
  private StringBuilder where(String conjunction) {
    if (where.length() > 0) {
      where.append(conjunction);
    }
    return where;
  }
}
