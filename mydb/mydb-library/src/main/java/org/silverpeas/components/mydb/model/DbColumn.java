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

import java.sql.JDBCType;

/**
 * A column in a database table. The column doesn't contain any values, it defines the column in
 * a {@link DbTable} instance by a name and a type.
 * @author mmoquillon
 */
public class DbColumn {

  private final int type;
  private final String name;
  private final boolean pk;
  private final int size;
  private final boolean nullable;

  /**
   * Constructs a new {@link DbColumn} instance of the specified SQL type and with the specified
   * name.
   * @param descriptor a descriptor of the column.
   */
  public DbColumn(final JdbcRequester.ColumnDescriptor descriptor) {
    this.type = descriptor.getType();
    this.name = descriptor.getName();
    this.size = descriptor.getSize();
    this.pk = descriptor.isPrimaryKey();
    this.nullable = descriptor.isNullable();
  }

  /**
   * Gets the SQL type of the values this column can contain. The values are define by the
   * {@link java.sql.Types} class.
   * @return an integer identifying a generic SQL type.
   */
  public int getType() {
    return type;
  }

  /**
   * Gets the name of this column.
   * @return the column's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the name of the SQL type of the values this column can contain. The name is determined
   * from the SQL type code by using the {@link JDBCType} class.
   * @return the name of the column's SQL type (VARCHAR, TIMESTAMP, ...)
   */
  public String getTypeName() {
    return JDBCType.valueOf(this.type).getName();
  }

  /**
   * Gets the size of this column. It depends on its type; for example for VARCHAR(256), its
   * size is 256.
   * @return the size of this column according to its type.
   */
  public int getSize() {
    return size;
  }

  /**
   * Is this column represents a primary key?
   * @return true if the values in this column are or are part of a primary key. False otherwise.
   */
  public boolean isPrimaryKey() {
    return pk;
  }

  /**
   * Is this column accepts null values?
   * @return true if this column is nullable. False otherwise.
   */
  public boolean isNullable() {
    return nullable;
  }

  /**
   * Is this column is of type text (VARCHAR, CLOB, ...)?
   * @return true if the values of this column are textual. False otherwise.
   */
  public boolean isOfTypeText() {
    return SqlTypes.isText(type);
  }

  /**
   * Is this column is of type binary (BLOB, VARBINARY, ...)?
   * @return true if the values of this column are binary. False otherwise.
   */
  public boolean isOfTypeBinary() {
    return SqlTypes.isBinary(type);
  }

  /**
   * Is this column of type number (INTEGER, BIGDECIMAL, ...)?
   * @return true if the values of this column are numbers. False otherwise.
   */
  public boolean isOfTypeNumber() {
    return SqlTypes.isBigInteger(type) || SqlTypes.isDouble(type) || SqlTypes.isFloat(type) ||
        SqlTypes.isInteger(type) || SqlTypes.isDecimal(type);
  }

  /**
   * Is this column of type date time (TIMESTAMP, TIMESTAMP_WITH_TIMEZONE, ...)?
   * @return true if the values of this column are date times. False otherwise.
   */
  public boolean isOfTypeDateTime() {
    return SqlTypes.isTimestamp(this.type);
  }
}
  