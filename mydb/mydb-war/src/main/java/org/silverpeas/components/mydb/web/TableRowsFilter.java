/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.components.mydb.web;

import org.silverpeas.components.mydb.model.DbColumn;
import org.silverpeas.components.mydb.model.predicates.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Filter of table rows by applying a {@link ColumnValuePredicate} predicate on them.
 * @author mmoquillon
 */
public class TableRowsFilter {

  /**
   * Default value when no comparator or no field name are set.
   */
  public static final String FIELD_NONE = "*";

  private static final Map<String, BiFunction<DbColumn, String, ColumnValuePredicate>>
      comparators = new LinkedHashMap<>(7);
  private String comparator = FIELD_NONE;
  private String fieldValue = EMPTY;
  private DbColumn field = null;

  /*
   * Set up the different {@link ColumnValuePredicate} instances supported by this filter.
   */
  static {
    comparators.put(FIELD_NONE, Identity::new);
    comparators.put("include", Inclusion::new);
    comparators.put("like", Like::new);
    comparators.put("=", Equality::new);
    comparators.put("!=", Inequality::new);
    comparators.put("<=", Inferiority::new);
    comparators.put(">=", Superiority::new);
    comparators.put("<", StrictInferiority::new);
    comparators.put(">", StrictSuperiority::new);
  }

  /**
   * Gets the symbol of all of the comparators supported by this filter.
   * @return a set of comparator symbols.
   */
  public static Set<String> getAllComparators() {
    return comparators.keySet();
  }

  /**
   * Sets the current comparator to use in the filtering of the SQL query result.
   * @param comparatorSymbol the symbol of a supported comparator.
   */
  public void setComparator(final String comparatorSymbol) {
    final boolean exists =
        comparators.containsKey(comparatorSymbol != null ? comparatorSymbol : FIELD_NONE);
    if (!exists) {
      throw new IllegalArgumentException("Comparator " + comparatorSymbol + " not supported!");
    }
    this.comparator = comparatorSymbol;
  }

  /**
   * Sets the column of the table on which the filtering will be applied.
   * @param column a {@link DbColumn} instance.
   */
  public void setColumn(final DbColumn column) {
    Objects.requireNonNull(column);
    this.field = column;
  }

  /**
   * Sets the value on which the whole column will be filtered.
   * @param value the value on which the filtering will be applied.
   */
  public void setColumnValue(final String value) {
    this.fieldValue = value != null ? value : EMPTY;
  }

  /**
   * Gets the symbol of the comparator used in the filtering of the SQL query result.
   * @return the symbol of a supported comparator.
   */
  public String getComparator() {
    return comparator;
  }

  /**
   * Ges the column on which the filtering will be applied.
   * @return optionally a {@link DbColumn} instance. If no filter was set, then returns nothing.
   */
  public Optional<DbColumn> getColumn() {
    return Optional.ofNullable(this.field);
  }

  /**
   * Gets the reference field value, that is the value with which the field of all table rows in
   * the result will be filtered.
   * @return the value used in the filtering as a {@link String} instance.
   */
  public String getColumnValue() {
    return fieldValue;
  }

  /**
   * Clears all the filtering parameters used by this filter.
   */
  public void clear() {
    this.comparator = FIELD_NONE;
    this.fieldValue = "";
    this.field = null;
  }

  /**
   * Is this filter defined?
   * @return true if the filtering parameters are set, false otherwise.
   */
  public boolean isDefined() {
    return !FIELD_NONE.equals(this.comparator) && this.field != null && !this.fieldValue.isEmpty();
  }

  /**
   * Gets the predicate corresponding to this filtering rule.
   * @return a {@link ColumnValuePredicate} object. If no filtering rule is defined, returns an
   * {@link Identity} predicate.
   */
  public ColumnValuePredicate getFilteringPredicate() {
    final ColumnValuePredicate predicate;
    if (isDefined()) {
      predicate = comparators.get(this.comparator).apply(this.field, this.fieldValue);
    } else {
      predicate = new Identity();
    }
    return predicate;
  }
}
  