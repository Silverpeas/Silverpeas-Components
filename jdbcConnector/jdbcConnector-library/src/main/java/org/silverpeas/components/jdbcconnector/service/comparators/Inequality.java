/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.jdbcconnector.service.comparators;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.components.jdbcconnector.service.comparators.Equality.EMPTY_VALUE;
import static org.silverpeas.components.jdbcconnector.service.comparators.Equality.NULL_VALUE;

/**
 * The inequality comparator
 * @author mmoquillon
 */
public class Inequality implements FieldValueComparator {

  @Override
  public boolean compare(final Comparable value, final Comparable referenceValue) {
    if (value == null && NULL_VALUE.equals(referenceValue)) {
      return false;
    } else if (value != null && !NULL_VALUE.equals(referenceValue)) {
      final String finalReferenceValue = EMPTY_VALUE.equals(referenceValue)
          ? EMPTY
          : referenceValue.toString();
      return value.toString().compareTo(finalReferenceValue) != 0;
    }
    return true;
  }
}
  