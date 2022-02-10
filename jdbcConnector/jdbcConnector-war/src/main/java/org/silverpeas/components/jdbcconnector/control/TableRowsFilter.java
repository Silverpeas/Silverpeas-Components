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

package org.silverpeas.components.jdbcconnector.control;

import org.silverpeas.components.jdbcconnector.service.TableRow;
import org.silverpeas.components.jdbcconnector.service.comparators.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Filter of table rows by applying a {@link FieldValueComparator} predicate on them.
 * @author mmoquillon
 */
public class TableRowsFilter {

  /**
   * Default value when no comparator or no field name are set.
   */
  public static final String FIELD_NONE = "*";

  private static final Map<String, FieldValueComparator> comparators = new LinkedHashMap<>(7);
  private String comparator = FIELD_NONE;
  private String fieldName = FIELD_NONE;
  private String fieldValue = EMPTY;
  private Class<?> fieldType = null;

  /**
   * Set up the different {@link FieldValueComparator} instances supported by this filter.
   */
  static {
    comparators.put(FIELD_NONE, new NothingBuilder());
    comparators.put("include", new Inclusion());
    comparators.put("like", new Like());
    comparators.put("=", new Equality());
    comparators.put("!=", new Inequality());
    comparators.put("<=", new Inferiority());
    comparators.put(">=", new Superiority());
    comparators.put("<", new StrictInferiority());
    comparators.put(">", new StrictSuperiority());
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
    FieldValueComparator builder = comparators
        .get(comparatorSymbol != null ? comparatorSymbol : FIELD_NONE);
    if (builder == null) {
      throw new IllegalArgumentException("Comparator " + comparatorSymbol + " not supported!");
    }
    this.comparator = comparatorSymbol;
  }

  /**
   * Sets the name of the field, and its concrete type, in each table row of a result to filter.
   * @param fieldName the name of a field in the table rows of a SQL query result.
   * @param fieldType the concrete type of the field.
   */
  public void setFieldName(final String fieldName, final Class<?> fieldType) {
    Objects.requireNonNull(fieldName);
    Objects.requireNonNull(fieldType);
    this.fieldName = fieldName;
    this.fieldType = fieldType;
  }

  /**
   * Sets the value with which a field of all of the table row will be filtered.
   * @param fieldValue the value with which the field of each table row will be compared.
   */
  public void setFieldValue(final String fieldValue) {
    this.fieldValue = fieldValue != null ? fieldValue : EMPTY;
  }

  /**
   * Gets the symbol of the comparator used in the filtering of the SQL query result.
   * @return the symbol of a supported comparator.
   */
  public String getComparator() {
    return comparator;
  }

  /**
   * Gets the name of the field to filter.
   * @return a field name.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Gets the reference field value, that is the value with which the field of all table rows in
   * the result will be filtered.
   * @return the value used in the filtering as a {@link String} instance.
   */
  public String getFieldValue() {
    return fieldValue;
  }

  /**
   * Clears all the filtering parameters used by this filter.
   */
  public void clear() {
    this.fieldName = FIELD_NONE;
    this.comparator = FIELD_NONE;
    this.fieldValue = "";
    this.fieldType = null;
  }

  /**
   * Filters the specified table rows by applying the underlying filtering parameters that were set
   * by the setters.
   * @param rows the rows to filter.
   * @return a list with all the table rows filtered according to the underlying filtering
   * parameters.
   */
  public List<TableRow> filter(final List<TableRow> rows) {
    final FieldValueComparator predicate = comparators.get(comparator);
    if (!fieldName.equals(FIELD_NONE) && predicate != null) {
      Comparable actualValue;
      try {
        Method valueOf = fieldType.getMethod("valueOf", String.class);
        actualValue = (Comparable) valueOf.invoke(fieldType, fieldValue);
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
          ClassCastException e) {
        actualValue = fieldValue;
      }
      final Comparable refValue = actualValue;
      return rows.stream()
          .filter(byApplying(predicate, fieldName, refValue))
          .collect(Collectors.toList());
    }
    return rows;
  }

  @SuppressWarnings("unchecked")
  private Predicate<TableRow> byApplying(final FieldValueComparator comparator,
      final String fieldName, final Comparable withValue) {
    return r -> {
      Comparable v = r.getFieldValue(fieldName);
      return withValue != null && comparator.compare(v, withValue);
    };
  }
}
  