/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mydb.data.index;

/**
 * IndexInfo element of a database table. Used to initialize the IndexInfo list of a table.
 * @author Antoine HEDIN
 */
public class IndexElement {

  public static final String COLUMN_NAME = "COLUMN_NAME";
  public static final String INDEX_NAME = "INDEX_NAME";
  public static final String ORDINAL_POSITION = "ORDINAL_POSITION";

  private String indexName;
  private String column;
  private short position;

  public IndexElement(String indexName, String column, short position) {
    this.column = column;
    this.indexName = indexName;
    this.position = position;
  }

  public String getIndexName() {
    return indexName;
  }

  public String getColumn() {
    return column;
  }

  public short getPosition() {
    return position;
  }

}
