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

package org.silverpeas.components.jdbcconnector.control;

import org.silverpeas.components.jdbcconnector.service.comparators.EqualityBuilder;
import org.silverpeas.components.jdbcconnector.service.comparators.FieldComparatorBuilder;
import org.silverpeas.components.jdbcconnector.service.comparators.InclusionBuilder;
import org.silverpeas.components.jdbcconnector.service.comparators.InequalityBuilder;
import org.silverpeas.components.jdbcconnector.service.comparators.InferiorityBuilder;
import org.silverpeas.components.jdbcconnector.service.comparators.NothingBuilder;
import org.silverpeas.components.jdbcconnector.service.comparators.StrictInferiorityBuilder;
import org.silverpeas.components.jdbcconnector.service.comparators.SuperiorityBuilder;
import org.silverpeas.core.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author mmoquillon
 */
public class SQLQueryBuilder {

  /**
   * Default value when no comparator or no field name are set.
   */
  public static final String FIELD_NONE = "*";

  private static final Map<String, FieldComparatorBuilder> comparators = new LinkedHashMap<>(7);
  private String comparator = FIELD_NONE;
  private String fieldName = FIELD_NONE;
  private String fieldValue = "";
  private Class<?> fieldType = null;
  private String query;

  static {
    comparators.put(FIELD_NONE, new NothingBuilder());
    comparators.put("include", new InclusionBuilder());
    comparators.put("=", new EqualityBuilder());
    comparators.put("!=", new InequalityBuilder());
    comparators.put("<=", new InferiorityBuilder());
    comparators.put(">=", new SuperiorityBuilder());
    comparators.put("<", new StrictInferiorityBuilder());
    comparators.put(">", new StrictInferiorityBuilder());
  }

  /**
   * Gets the symbol of all of the comparators supported by this filter.
   * @return a set of comparator symbols.
   */
  public static Set<String> getAllComparators() {
    return comparators.keySet();
  }

  public void setComparator(final String comparatorSymbol) {
    FieldComparatorBuilder builder = comparators.get(comparatorSymbol);
    if (builder == null) {
      throw new IllegalArgumentException("Comparator " + comparatorSymbol + " not supported!");
    }
    this.comparator = comparatorSymbol;
  }

  public void setQuery(final String query) {
    Objects.requireNonNull(query);
    this.query = query;
  }

  public void setFieldName(final String fieldName, final Class<?> fieldType) {
    Objects.requireNonNull(fieldName);
    Objects.requireNonNull(fieldType);
    this.fieldName = fieldName;
    this.fieldType = fieldType;
  }

  public void setFieldValue(final String fieldValue) {
    Objects.requireNonNull(fieldValue);
    this.fieldValue = fieldValue;
  }

  public String getComparator() {
    return comparator;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getFieldValue() {
    return fieldValue;
  }

  public void clear() {
    this.fieldName = FIELD_NONE;
    this.comparator = FIELD_NONE;
    this.fieldValue = "";
    this.fieldType = null;
  }

  public String build() {
    final String field = fieldName.equals(FIELD_NONE) ? "" : fieldName;
    final String comparingStatement =
        comparators.get(comparator).compare(field, fieldValue, fieldType);
    if (StringUtil.isDefined(comparingStatement)) {
      final String where = "where";
      int idx = query.indexOf(where);
      if (idx > 0) {
        int insertionIdx = idx + where.length();
        return query.substring(0, insertionIdx) + " " + comparingStatement + " and" +
            query.substring(insertionIdx);
      }
      return query + " where " + comparingStatement;

    }
    return query;
  }
}
  