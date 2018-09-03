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

package org.silverpeas.components.mydb.model;

import org.silverpeas.components.mydb.service.MyDBRuntimeException;

import java.util.Objects;

/**
 * The value of a field in a table row with its type (and associated type name) in the database.
 * @author mmoquillon
 */
public class TableFieldValue implements Comparable<TableFieldValue> {

  private final Object value;
  private final String typeName;
  private final int type;

  TableFieldValue(final Object value, final int type, final String typeName) {
    this.value = value;
    this.type = type;
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }

  public int getType() {
    return type;
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
    return Objects.equals(value, that.value) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, type);
  }

  /**
   * Compares this {@link TableFieldValue} with the specified one. The comparing is actually done
   * on the wrapped values themselves. If the wrapped values satisfy the {@link Comparable}
   * interface then the {@link Comparable#compareTo(Object)} method is used, otherwise both of them
   * are converted in {@link String} objects and these {@link String} instances are then compared
   * between themselves.
   * @param o another {@link TableFieldValue} with which this one is compared.
   * @return the comparing distance between the two {@link TableFieldValue} instances.
   */
  @Override
  public int compareTo(final TableFieldValue o) {
    if (this.type != o.type) {
      throw new MyDBRuntimeException(
          "The two table field values aren't of the same type: this is of type " + this.typeName +
              " whereas other if of type " + o.typeName);
    }
    final int compare;
    if (o.value == null && this.value == null) {
      compare = 0;
    } else if (this.value instanceof Comparable) {
      compare = ((Comparable) this.value).compareTo(o.value);
    } else if (o.value == null) {
      return 1;
    } else if (this.value == null) {
      return -1;
    } else {
      compare = toString().compareTo(o.toString());
    }
    return compare;
  }

  @Override
  public String toString() {
    return value == null ? null : value.toString();
  }
}
