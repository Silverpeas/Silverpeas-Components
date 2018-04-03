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

package org.silverpeas.components.jdbcconnector.service.comparators;

import org.silverpeas.components.jdbcconnector.service.TableRow;

/**
 * A comparator of a field in a table row with a given value.
 * @author mmoquillon
 */
public interface FieldComparatorBuilder {

  /**
   * Builds the comparator that compares the specified field of a table row with the specified
   * value.
   * @param fieldName the name of a field of a {@link TableRow} to compare.
   * @param value the value with which the field has to be compared.
   * @return the comparing statement according to the comparator this builder builds.
   */
  String compare(final String fieldName, final String value, final Class<?> type);
}
  