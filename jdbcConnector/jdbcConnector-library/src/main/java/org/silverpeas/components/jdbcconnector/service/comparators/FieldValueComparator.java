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

package org.silverpeas.components.jdbcconnector.service.comparators;

/**
 * A comparator of comparable values. Such comparator is a comparing predicate between two
 * comparable objects.
 * @author mmoquillon
 */
@FunctionalInterface
public interface FieldValueComparator {

  /**
   * Compares the value with the reference value. The two specified values must be comparable.
   * @param value the value to compare.
   * @param referenceValue the reference value the value is compared to.
   * @return true if the comparing predicate between the two specified values is satisfied.
   */
  boolean compare(final Comparable value, final Comparable referenceValue);
}
  