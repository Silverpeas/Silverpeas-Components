/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.mydb.model.predicates;

import org.silverpeas.components.mydb.model.DbColumn;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

/**
 * The inequality predicate.
 * @author mmoquillon
 */
public class Inequality extends AbstractColumnValuePredicate {

  public Inequality(final DbColumn column, final String refValue) {
    super(column, refValue);
  }

  @Override
  public JdbcSqlQuery apply(final JdbcSqlQuery query) {
    final JdbcSqlQuery q;
    final Object value = getNormalizedValue();
    if (value == null) {
      q = query.where(getColumn().getName() + " is not null");
    } else if (getColumn().isOfTypeText()) {
      q = query.where("lower(" + getColumn().getName() + ") != lower(?)", value);
    } else {
      q = query.where(getColumn().getName() + " != ?", value);
    }
    return q;
  }
}
