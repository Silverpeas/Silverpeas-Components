/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import com.silverpeas.mydb.data.db.DbColumn;

/**
 * Table foreign key.
 * @author Antoine HEDIN
 */
public class ForeignKey {

  public static final String FOREIGN_KEY_PREFIX = "fk_";

  private String name;
  private String[] columns;
  private String foreignTable;
  private DbColumn[] foreignColumns;

  public ForeignKey(String name, String[] columns, String foreignTable,
      DbColumn[] foreignColumns) {
    this.name = name;
    this.columns = columns;
    this.foreignTable = foreignTable;
    this.foreignColumns = foreignColumns;
  }

  public String getName() {
    return name;
  }

  public void setColumns(String[] columns) {
    this.columns = columns;
  }

  public String[] getColumns() {
    return columns;
  }

  public String getColumn(int index) {
    return columns[index];
  }

  public String getForeignTable() {
    return foreignTable;
  }

  public DbColumn[] getForeignColumns() {
    return foreignColumns;
  }

  public DbColumn getForeignColumn(int index) {
    return foreignColumns[index];
  }

  public int getColumnsCount() {
    return columns.length;
  }

  public int getForeignColumnsCount() {
    return foreignColumns.length;
  }

  public boolean containsColumn(String name) {
    for (int i = 0, n = getColumnsCount(); i < n; i++) {
      if (columns[i].equals(name)) {
        return true;
      }
    }
    return false;
  }

  public String[] getForeignColumnsNames() {
    int n = getForeignColumnsCount();
    String[] columnsNames = new String[n];
    for (int i = 0; i < n; i++) {
      columnsNames[i] = getForeignColumn(i).getName();
    }
    return columnsNames;
  }

  public DbColumn getLinkedForeignColumn(String name) {
    for (int i = 0, n = getColumnsCount(); i < n; i++) {
      if (getColumn(i).equals(name)) {
        return getForeignColumn(i);
      }
    }
    return null;
  }

  public void replace(String oldColumnName, String newColumnName) {
    int columnsCount = getColumnsCount();
    String[] newColumns = new String[columnsCount];
    for (int i = 0; i < columnsCount; i++) {
      newColumns[i] = (columns[i].equals(oldColumnName) ? newColumnName
          : columns[i]);
    }
    setColumns(newColumns);
  }

}
