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

package org.silverpeas.components.jdbcconnector.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * The value of a field in a table row.
 * @author mmoquillon
 */
public class TableFieldValue implements Comparable<TableFieldValue> {

  private final Object value;

  TableFieldValue(final Object value) {
    this.value = value;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TableFieldValue that = (TableFieldValue) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public int compareTo(final TableFieldValue o) {
    if (o.value == null && value == null) {
      return 0;
    } else if (o.value == null) {
      return 1;
    } else if (value == null) {
      return -1;
    } else if (o.value.getClass().equals(value.getClass())) {
      try {
        Method m = o.getClass().getMethod("compareTo", o.getClass());
        return (Integer) m.invoke(value, o.value);
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        return toString().compareTo(o.toString());
      }
    }
    throw new IllegalArgumentException("The table field value isn't of the same type than this");
  }

  @Override
  public String toString() {
    return value == null ? null : value.toString();
  }
}
  