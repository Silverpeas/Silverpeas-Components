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
 * Abstract implementation of the {@link ColumnValuePredicate} interface.
 * @author mmoquillon
 */
public abstract class AbstractColumnValuePredicate implements ColumnValuePredicate {

  public static final String NULL_VALUE = "null";
  public static final String EMPTY_VALUE = "@empty@";
  private DbColumn column;
  private Comparable value;

  /**
   * Constructs a new predicate on the specified database table's column and with the given
   * reference value.
   * @param column the column.
   * @param refValue a reference value.
   */
  public AbstractColumnValuePredicate(final DbColumn column, final Comparable refValue) {
    this.column = column;
    this.value = refValue;
  }

  /**
   * Constructs an empty predicate. Dedicated to the concrete implementation of a predicate only
   * if they have to perform some specified tasks at initialization time.
   */
  AbstractColumnValuePredicate() {
    // dedicated to the implementors
  }

  final AbstractColumnValuePredicate setColumn(final DbColumn column) {
    this.column = column;
    return this;
  }

  final AbstractColumnValuePredicate setValue(final Comparable value) {
    this.value = value;
    return this;
  }

  @Override
  public DbColumn getColumn() {
    return this.column;
  }

  @Override
  public Comparable getReferenceValue() {
    return this.value;
  }

  public abstract JdbcSqlQuery apply(final JdbcSqlQuery query);
}
  