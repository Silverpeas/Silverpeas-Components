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

package org.silverpeas.components.mydb.model;

import org.silverpeas.components.mydb.service.MyDBRuntimeException;
import org.silverpeas.core.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * The value of a field in a table row with its type (and associated type name) in the database.
 * @author mmoquillon
 */
public class TableFieldValue implements Comparable<TableFieldValue> {

  private Object value;
  private final int type;

  /**
   * Constructs a {@link TableFieldValue} instance from the specified value represented as a
   * {@link String} object and according to the specified SQL type (a value among
   * {@link java.sql.Types}). If the specified value
   * doesn't match the SQL type of this field value, then an {@link IllegalArgumentException}
   * exception is thrown.
   * @param value the {@link String} representation of a value. Null value shouldn't be null but the
   * String "null".
   * @param sqlType the SQL type as defined in {@link java.sql.Types}.
   * @return a {@link TableFieldValue} instance.
   */
  public static TableFieldValue fromString(final String value, int sqlType) {
    TableFieldValue tableFieldValue = new TableFieldValue(null, sqlType);
    tableFieldValue.update(value);
    return tableFieldValue;
  }

  /**
   * Constructs a new value of a field in a database table.
   * @see java.sql.Types for SQL type of the value.
   * @param value the value of the field
   * @param type the SQL type of the value
   */
  TableFieldValue(final Object value, final int type) {
    this.value = value;
    this.type = type;
  }

  /**
   * Gets the code of the SQL type of this value.
   * @see java.sql.Types for the available possible codes.
   * @return the SQL type of this value as defined in {@link java.sql.Types}.
   */
  public int getType() {
    return type;
  }

  /**
   * Is this value a text?
   * @return true if the type of this value is a text, false otherwise.
   */
  public boolean isText() {
    return SqlTypes.isText(this.type);
  }

  /**
   * Is this value an empty text?
   * @return true if this value is a text and it is empty. False otherwise.
   */
  public boolean isEmpty() {
    return isText() && toString().isEmpty();
  }

  /**
   * Updates this value with the textual representation of the new value. If the specified value
   * doesn't match the SQL type of this field value, then an {@link IllegalArgumentException}
   * exception is thrown.
   * @param value a {@link String} representation of the value. Null value shouldn't be null but the
   * String "null".
   */
  public void update(final String value) {
    if (value == null || value.equals("null")) {
      this.value = null;
    } else if (SqlTypes.isText(this.type)) {
      this.value = value;
    } else if (SqlTypes.isDate(this.type)) {
      try {
        this.value = Date.valueOf(value);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("The date '" + value +
            "' is not in the database date escape format (yyyy-[m]m-[d]d)");
      }
    } else if (SqlTypes.isTime(this.type)) {
      this.value = Time.valueOf(value);
    } else if (SqlTypes.isTimestamp(this.type)) {
      this.value = Timestamp.valueOf(value);
    } else if (SqlTypes.isBoolean(this.type)) {
      this.value = StringUtil.getBooleanValue(value);
    } else if (SqlTypes.isBigInteger(this.type)) {
      this.value = BigInteger.valueOf(Long.valueOf(value));
    } else if (SqlTypes.isDecimal(this.type)) {
      this.value = BigDecimal.valueOf(Double.valueOf(value));
    } else if (SqlTypes.isInteger(this.type)) {
      this.value = Integer.valueOf(value);
    } else if (SqlTypes.isFloat(this.type)) {
      this.value = Float.valueOf(value);
    } else if (SqlTypes.isDouble(this.type)) {
      this.value = Double.valueOf(value);
    }
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
      final String typeName = JDBCType.valueOf(this.type).getName();
      throw new MyDBRuntimeException(
          "The two table field values aren't of the same type: this is of type " + typeName +
              " whereas other if of type " + typeName);
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

  /**
   * Converts this table field value to an SQL object as defined by its SQL type.
   * @return this value as an SQL object.
   */
  Object toSQLObject() {
    return value;
  }

  @Override
  public String toString() {
    return value == null ? "null" : value.toString();
  }

  public TableFieldValue getCopy() {
    return new TableFieldValue(value, type);
  }
}
