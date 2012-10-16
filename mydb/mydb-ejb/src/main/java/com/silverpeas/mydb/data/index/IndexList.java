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

package com.silverpeas.mydb.data.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.silverpeas.mydb.data.datatype.DataTypeList;
import com.silverpeas.mydb.data.db.DbColumn;
import com.silverpeas.mydb.data.db.DbColumnComparator;

/**
 * IndexInfo list of a database table.
 * @author Antoine HEDIN
 */
public class IndexList {

  private ArrayList<IndexInfo> indexInfos;
  private ArrayList<DbColumn> columns;

  public IndexList() {
    indexInfos = new ArrayList<IndexInfo>();
    columns = new ArrayList<DbColumn>();
  }

  public void addIndexInfo(IndexInfo indexInfo) {
    indexInfos.add(indexInfo);
  }

  public IndexInfo getIndexInfo(int index) {
    return indexInfos.get(index);
  }

  public IndexInfo getIndexInfo(String name) {
    IndexInfo indexInfo;
    for (int i = 0, n = getIndexInfosCount(); i < n; i++) {
      indexInfo = getIndexInfo(i);
      if (indexInfo.getName().equals(name)) {
        return indexInfo;
      }
    }
    return null;
  }

  public int getIndexInfosCount() {
    return indexInfos.size();
  }

  public boolean containsIndexInfo(String name) {
    for (int i = 0, n = getIndexInfosCount(); i < n; i++) {
      if (getIndexInfo(i).getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public int getIndexInfoMaxColumnsCount() {
    int max = 1;
    for (int i = 0, n = getIndexInfosCount(); i < n; i++) {
      max = Math.max(getIndexInfo(i).getColumnsCount(), max);
    }
    return max;
  }

  public void addColumn(DbColumn column) {
    columns.add(column);
  }

  public DbColumn getColumn(int index) {
    return columns.get(index);
  }

  public DbColumn getColumn(String name) {
    DbColumn column;
    for (int i = 0, n = getColumnsCount(); i < n; i++) {
      column = getColumn(i);
      if (column.getName().equals(name)) {
        return column;
      }
    }
    return null;
  }

  public int getColumnsCount() {
    return columns.size();
  }

  public boolean containsColumn(String name) {
    for (int i = 0, n = getColumnsCount(); i < n; i++) {
      if (getColumn(i).getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public void sortColumns() {
    Collections.sort(columns, new DbColumnComparator());
  }

  public DbColumn[] getColumns() {
    return (DbColumn[]) columns.toArray(new DbColumn[columns.size()]);
  }

  public void check(DataTypeList dataTypeList) {
    IndexInfo indexInfo;
    String[] columnsNames;
    int columnsNamesCount;
    DbColumn column;
    boolean keepIndexInfo;
    Hashtable<String, String> keptColumnsNames = new Hashtable<String, String>();
    for (int i = (getIndexInfosCount() - 1); i >= 0; i--) {
      indexInfo = getIndexInfo(i);
      columnsNames = indexInfo.getColumns();
      columnsNamesCount = columnsNames.length;
      keepIndexInfo = true;
      for (int j = 0; j < columnsNamesCount; j++) {
        column = getColumn(columnsNames[j]);
        keepIndexInfo = (keepIndexInfo && dataTypeList.contains(column
            .getDataType()));
      }
      if (keepIndexInfo) {
        for (int j = 0; j < columnsNamesCount; j++) {
          keptColumnsNames.put(columnsNames[j], "");
        }
      } else {
        indexInfos.remove(i);
      }
    }
    for (int i = (getColumnsCount() - 1); i >= 0; i--) {
      column = getColumn(i);
      if (keptColumnsNames.containsKey(column.getName())) {
        if (!dataTypeList.isSizeEnabled(column.getDataType())) {
          column.removeDataSize();
        }
      } else {
        columns.remove(i);
      }
    }
  }

}