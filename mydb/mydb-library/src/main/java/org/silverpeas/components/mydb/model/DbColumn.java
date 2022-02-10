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

import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.JDBCType;
import java.sql.Types;
import java.util.Objects;
import java.util.stream.Stream;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A column in a database table. The column doesn't contain any values, it defines the column in
 * a {@link DbTable} instance by a name and a type.
 * @author mmoquillon
 */
public class DbColumn {

  private JdbcRequester.ColumnDescriptor descriptor;

  /**
   * Constructs a new {@link DbColumn} instance of the specified SQL type and with the specified
   * name.
   * @param descriptor a descriptor of the column.
   */
  public DbColumn(final JdbcRequester.ColumnDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  /**
   * Gets the SQL type of the values this column can contain. The values are define by the
   * {@link java.sql.Types} class.
   * @return an integer identifying a generic SQL type.
   */
  public int getType() {
    return descriptor.getType();
  }

  /**
   * Gets the name of this column.
   * @return the column's name.
   */
  public String getName() {
    return descriptor.getName();
  }

  /**
   * Gets the name of the SQL type of the values this column can contain. The name is determined
   * from the SQL type code by using the {@link JDBCType} class.
   * @return the name of the column's SQL type (VARCHAR, TIMESTAMP, ...)
   */
  public String getTypeName() {
    return JDBCType.valueOf(getType()).getName();
  }

  /**
   * Gets the size of this column. It depends on its type; for example for VARCHAR(256), its
   * size is 256.
   * @return the size of this column according to its type.
   */
  public int getSize() {
    int size = descriptor.getSize();
    if (getType() == Types.BIT && descriptor.getSize() == 1) {
      size = Math.max("true".length(), "false".length());
    }
    return size;
  }

  /**
   * Is this column represents a primary key?
   * @return true if the values in this column are or are part of a primary key. False otherwise.
   */
  public boolean isPrimaryKey() {
    return descriptor.isPrimaryKey();
  }

  /**
   * Is this column references a column of another table. If true, then the column takes part of
   * a foreign key whose name is given by the {@link DbColumn#getName()} method.
   * @return true if the values in this column are the primary keys of another table. False
   * otherwise.
   */
  public boolean isForeignKey() {
    return descriptor.getForeignKey() != null;
  }

  /**
   * Gets the name of the foreign key to which this column is a component. Several columns can
   * be part of the same foreign key; in this case, the foreign key name is a way to figure out
   * them.
   * If this column isn't a foreign key, then {@link NullPointerException} is thrown.
   * @return the unique foreign key name.
   */
  public String getForeignKeyName() {
    Objects.requireNonNull(descriptor.getForeignKey());
    return descriptor.getForeignKey().getName();
  }

  /**
   * Gets the column that is referenced by this column if this column is a foreign key.
   * If this column isn't a foreign key, then {@link NullPointerException} is thrown.
   * @return the name of the column referenced by this foreign key.
   */
  public String getReferencedColumn() {
    Objects.requireNonNull(descriptor.getForeignKey());
    return descriptor.getForeignKey().getTargetColumnName();
  }

  /**
   * Gets the table that is referenced by this column if this column is a foreign key.
   * If this column isn't a foreign key, then {@link NullPointerException} is thrown.
   * @return the name of the table referenced by this foreign key.
   */
  public String getReferencedTable() {
    Objects.requireNonNull(descriptor.getForeignKey());
    return descriptor.getForeignKey().getTargetTableName();
  }

  /**
   * Is this column accepts null values?
   * @return true if this column is nullable. False otherwise.
   */
  public boolean isNullable() {
    return descriptor.isNullable();
  }

  /**
   * Is the valuation of this column autogenerated when a new tuple is inserted into the table?
   * @return true if this column is auto-valuated by the database when a row is newly inserted into
   * the table this column belongs to.
   */
  public boolean isAutoValued() {
    return descriptor.isAutoIncrementable();
  }

  /**
   * Is there a default value defined for this column?
   * @return true if a default value is defined for this column, false otherwise.
   */
  public boolean isDefaultValueDefined() {
    return this.descriptor.getDefaultValue().isDefined();
  }

  /**
   * Gets the String representation of the value by default of this column when no one is
   * explicitly set.
   * @return as a {@link String} the default value of this column.
   */
  public String getDefaultValue() {
    return this.descriptor.getDefaultValue().get();
  }

  /**
   * Is this column is of type text (VARCHAR, CLOB, ...)?
   * @return true if the values of this column are textual. False otherwise.
   */
  public boolean isOfTypeText() {
    return SqlTypes.isText(getType());
  }

  /**
   * Is this column is of type binary (BLOB, VARBINARY, ...)?
   * @return true if the values of this column are binary. False otherwise.
   */
  public boolean isOfTypeBinary() {
    return SqlTypes.isBinary(getType());
  }

  /**
   * Is this column of type number (INTEGER, BIGDECIMAL, ...)?
   * @return true if the values of this column are numbers. False otherwise.
   */
  public boolean isOfTypeNumber() {
    final int type = getType();
    return SqlTypes.isBigInteger(type) || SqlTypes.isDouble(type) || SqlTypes.isFloat(type) ||
        SqlTypes.isInteger(type) || SqlTypes.isDecimal(type);
  }

  /**
   * Is this column of type date time (TIMESTAMP, TIMESTAMP_WITH_TIMEZONE, ...)?
   * @return true if the values of this column are date times. False otherwise.
   */
  public boolean isOfTypeDateTime() {
    return SqlTypes.isTimestamp(getType());
  }

  /**
   * Gets a JDBC value of given value. The aim is to get a value to fill into
   * JDBC query criteria filters.
   * <p>
   * Comma character is used to define a list of values.
   * </p>
   * @param value a value to convert.
   * @return a JDBC value according to the SQL type of the column.
   */
  public Object getJdbcValueOf(final String value) {
    try {
      if (isDefined(value)) {
        final Object[] values = Stream
            .of(value.replace("\\,", "\\@@##@@").split(","))
            .map(v -> v.replace("\\@@##@@", "\\,"))
            .map(v -> TableFieldValue.fromString(v, getType()).toSQLObject())
            .toArray();
        return values.length == 1 ? values[0] : values;
      }
    } catch (final Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return value;
  }
}
  