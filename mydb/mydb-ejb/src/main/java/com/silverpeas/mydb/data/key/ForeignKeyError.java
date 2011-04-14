/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mydb.data.key;

/**
 * Error linked to a table foreign key.
 * @author Antoine HEDIN
 */
public class ForeignKeyError {

  public static final int ERROR_TYPE = 0;
  public static final int ERROR_SIZE = 1;

  private String column;
  private String label;
  private int type;
  private int correctedValue;

  public ForeignKeyError(String column, String label, int type,
      int correctedValue) {
    this.column = column;
    this.label = label;
    this.type = type;
    this.correctedValue = correctedValue;
  }

  public String getColumn() {
    return column;
  }

  public String getLabel() {
    return label;
  }

  public int getType() {
    return type;
  }

  public int getCorrectedValue() {
    return correctedValue;
  }

}
