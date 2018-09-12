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

import java.sql.Types;

/**
 * @author mmoquillon
 */
public class SqlTypes {

  private SqlTypes() {

  }

  public static boolean isText(final int type) {
    return type == Types.CLOB || type == Types.NCLOB || type == Types.VARCHAR ||
        type == Types.LONGNVARCHAR || type == Types.LONGVARCHAR || type == Types.NVARCHAR;
  }

  public static boolean isBinary(final int type) {
    return type == Types.BLOB ||type == Types.VARBINARY;
  }

  public static boolean isDate(final int type) {
    return type == Types.DATE;
  }

  public static boolean isTime(final int type) {
    return type == Types.TIME || type == Types.TIME_WITH_TIMEZONE;
  }

  public static boolean isTimestamp(final int type) {
    return type == Types.TIMESTAMP || type == Types.TIMESTAMP_WITH_TIMEZONE;
  }

  public static boolean isInteger(final int type) {
    return type == Types.INTEGER;
  }

  public static boolean isFloat(final int type) {
    return type == Types.FLOAT;
  }

  public static boolean isDecimal(final int type) {
    return type == Types.DECIMAL;
  }

  public static boolean isDouble(final int type) {
    return type == Types.DOUBLE;
  }

  public static boolean isBoolean(final int type) {
    return type == Types.BOOLEAN;
  }

  public static boolean isBigInteger(final int type) {
    return type == Types.BIGINT;
  }
}
  