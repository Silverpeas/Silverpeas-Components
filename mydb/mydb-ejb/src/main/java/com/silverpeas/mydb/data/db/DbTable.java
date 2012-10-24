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

package com.silverpeas.mydb.data.db;

import java.util.ArrayList;
import java.util.Hashtable;

import com.silverpeas.mydb.data.key.PrimaryKey;

/**
 * Database table.
 * @author Antoine HEDIN
 */
public class DbTable {

  private String name;
  private PrimaryKey primaryKey;
  private ArrayList<DbColumn> columns;
  private ArrayList<DbLine> lines;
  private int lineIndex;

  private Hashtable<String, Integer> columnsIndexes;

  public DbTable(String name) {
    this.name = name;
    columns = new ArrayList<DbColumn>();
    columnsIndexes = new Hashtable<String, Integer>();
    lines = new ArrayList<DbLine>();
    primaryKey = new PrimaryKey(this);
    lineIndex = -1;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setLineIndex(int lineIndex) {
    this.lineIndex = lineIndex;
  }

  public int getLineIndex() {
    return lineIndex;
  }

  public void addColumn(DbColumn column) {
    columns.add(column);
    columnsIndexes.put(column.getName(), new Integer(columns.size() - 1));
  }

  public void addLine(DbLine line) {
    lines.add(line);
  }

  public DbColumn[] getColumns() {
    return (DbColumn[]) columns.toArray(new DbColumn[columns.size()]);
  }

  public DbColumn getColumn(int index) {
    return columns.get(index);
  }

  public DbColumn getColumn(String name) {
    return columns.get(getColumnIndex(name));
  }

  public int getColumnIndex(String name) {
    return (columnsIndexes.get(name)).intValue();
  }

  public void removeColumn(int index) {
    primaryKey.removeColumn(getColumn(index).getName());
    columns.remove(index);
    reloadColumnsIndexes();
  }

  private void reloadColumnsIndexes() {
    columnsIndexes.clear();
    for (int i = 0, n = columns.size(); i < n; i++) {
      columnsIndexes.put(getColumn(i).getName(), new Integer(i));
    }
  }

  public DbLine[] getLines() {
    return (DbLine[]) lines.toArray(new DbLine[lines.size()]);
  }

  public String[] getColumnsNames() {
    final int columnsCount = columns.size();
    String[] result = new String[columnsCount];
    for (int i = 0; i < columnsCount; i++) {
      result[i] = (columns.get(i)).getName();
    }
    return result;
  }

  public String[] getColumnsNames(boolean autoIncrementFilter) {
    if (autoIncrementFilter) {
      ArrayList<String> columnsNamesList = new ArrayList<String>();
      final int columnsCount = columns.size();
      DbColumn dbColumn;
      for (int i = 0; i < columnsCount; i++) {
        dbColumn = (DbColumn) columns.get(i);
        if (!dbColumn.isAutoIncrement()) {
          columnsNamesList.add(dbColumn.getName());
        }
      }
      return (String[]) columnsNamesList.toArray(new String[columnsNamesList
          .size()]);
    } else {
      return getColumnsNames();
    }
  }

  public DbLine getSelectedLine() {
    return (DbLine) lines.get(lineIndex);
  }

  public DbColumn[] getColumnsWithExportedForeignKeys() {
    ArrayList<DbColumn> resultList = new ArrayList<DbColumn>();
    DbColumn column;
    for (int i = 0, n = columns.size(); i < n; i++) {
      column = columns.get(i);
      if (column.hasExportedForeignKeys()) {
        resultList.add(column);
      }
    }
    return (DbColumn[]) (resultList.toArray(new DbColumn[resultList.size()]));
  }

  public PrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public String[][] getForeignKeyColumnsNames(String keyName) {
    ArrayList<String[]> columnsNames = new ArrayList<String[]>();
    DbColumn column;
    DbForeignKey foreignKey;
    for (int i = 0, n = columns.size(); i < n; i++) {
      column = columns.get(i);
      if (column.hasImportedForeignKey()) {
        foreignKey = column.getImportedForeignKey();
        if (foreignKey.getKeyName().equals(keyName)) {
          columnsNames.add(new String[] { column.getName(),
              foreignKey.getColumnName() });
        }
      }
    }
    return (String[][]) (columnsNames
        .toArray(new String[columnsNames.size()][2]));
  }

  public void forceColumnNotNull(String columnName) {
    getColumn(columnName).setNullable(false);
  }

  public void forceColumnsNotNull(String[] columnsNames) {
    for (int i = 0, n = columnsNames.length; i < n; i++) {
      getColumn(columnsNames[i]).setNullable(false);
    }
  }

  public void forceColumnNoDefaultValue(String columnName) {
    getColumn(columnName).setDefaultValue(null);
  }

  public void forceColumnsNoDefaultValue(String[] columnsNames) {
    for (int i = 0, n = columnsNames.length; i < n; i++) {
      getColumn(columnsNames[i]).setDefaultValue(null);
    }
  }

}