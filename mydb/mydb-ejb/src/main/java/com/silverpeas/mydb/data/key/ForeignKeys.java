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

package com.silverpeas.mydb.data.key;

import java.text.MessageFormat;
import java.util.ArrayList;

import com.silverpeas.mydb.data.datatype.DataType;
import com.silverpeas.mydb.data.datatype.DataTypeList;
import com.silverpeas.mydb.data.db.DbColumn;
import com.silverpeas.mydb.data.db.DbTable;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.util.ResourcesWrapper;

/**
 * Table foreign keys list.
 * @author Antoine HEDIN
 */
public class ForeignKeys {

  private ArrayList<ForeignKey> foreignKeys;
  private DbTable parentTable;

  public ForeignKeys(DbTable parentTable) {
    foreignKeys = new ArrayList<ForeignKey>();
    this.parentTable = parentTable;
  }

  public boolean isEmpty() {
    return (foreignKeys.size() == 0);
  }

  public int getSize() {
    return foreignKeys.size();
  }

  public void add(ForeignKey foreignKey) {
    foreignKeys.add(foreignKey);
  }

  public void remove(int index) {
    foreignKeys.remove(index);
  }

  public ForeignKey get(int index) {
    return (ForeignKey) foreignKeys.get(index);
  }

  public ForeignKey[] getList(String columnName) {
    ArrayList<ForeignKey> list = new ArrayList<ForeignKey>();
    ForeignKey foreignKey;
    for (int i = 0, n = getSize(); i < n; i++) {
      foreignKey = get(i);
      if (foreignKey.containsColumn(columnName)) {
        list.add(foreignKey);
      }
    }
    return (ForeignKey[]) list.toArray(new ForeignKey[list.size()]);
  }

  public String getConstraintName() {
    String tableName = parentTable.getName();
    if (StringUtil.isDefined(tableName)) {
      ArrayList<String> uKeys = new ArrayList<String>();
      for (int i = 0, n = getSize(); i < n; i++) {
        uKeys.add(get(i).getName());
      }
      int foreignIndex = 1;
      tableName = tableName.toLowerCase();
      String foreignKey = ForeignKey.FOREIGN_KEY_PREFIX + tableName + "_"
          + foreignIndex;
      while (uKeys.contains(foreignKey)) {
        foreignIndex++;
        foreignKey = ForeignKey.FOREIGN_KEY_PREFIX + tableName + "_"
            + foreignIndex;
      }
      return foreignKey;
    } else {
      return "";
    }
  }

  public void update(ForeignKey foreignKey, int index) {
    if (index == -1) {
      add(foreignKey);
    } else {
      foreignKeys.set(index, foreignKey);
    }
    parentTable.forceColumnsNoDefaultValue(foreignKey.getColumns());
  }

  public void replace(String oldColumnName, String newColumnName) {
    for (int i = 0, n = getSize(); i < n; i++) {
      get(i).replace(oldColumnName, newColumnName);
    }
  }

  public void remove(String oldColumnName) {
    for (int i = (getSize() - 1); i >= 0; i--) {
      if (get(i).containsColumn(oldColumnName)) {
        remove(i);
      }
    }
  }

  public boolean isForeignKey(String name) {
    for (int i = 0, n = getSize(); i < n; i++) {
      if (get(i).containsColumn(name)) {
        return true;
      }
    }
    return false;
  }

  public ForeignKeyError[] getErrors(DataTypeList dataTypeList,
      ResourcesWrapper resources) {
    ArrayList<ForeignKeyError> errors = new ArrayList<ForeignKeyError>();
    String label;
    String columnName;
    DbColumn column;
    ForeignKey foreignKey;
    DbColumn foreignColumn;
    DataType dataType;
    for (int i = 0, n = getSize(); i < n; i++) {
      foreignKey = get(i);
      for (int j = 0, m = foreignKey.getColumnsCount(); j < m; j++) {
        columnName = foreignKey.getColumn(j);
        column = parentTable.getColumn(columnName);
        foreignColumn = foreignKey.getForeignColumn(j);
        dataType = dataTypeList.get(column.getDataType());
        if (column.getDataType() != foreignColumn.getDataType()) {
          label = MessageFormat.format(resources
              .getString("ErrorForeignKeyTypes"), new String[] {
              foreignKey.getName(), columnName, dataType.getName(),
              foreignColumn.getName(), foreignKey.getForeignTable(),
              dataTypeList.getDataTypeName(foreignColumn.getDataType()) });
          errors.add(new ForeignKeyError(columnName, label,
              ForeignKeyError.ERROR_TYPE, foreignColumn.getDataType()));
        }
        if (column.getDataSize() != foreignColumn.getDataSize()
            && dataType.isSizeEnabled()) {
          String columnSize = (column.hasDataSize() ? column
              .getDataSizeAsString() : resources.getString("UndefinedFemale"));
          String foreignColumnSize = (foreignColumn.hasDataSize() ? foreignColumn
              .getDataSizeAsString()
              : resources.getString("UndefinedFemale"));
          label = MessageFormat.format(resources
              .getString("ErrorForeignKeySizes"), new String[] {
              foreignKey.getName(), columnName, columnSize,
              foreignColumn.getName(), foreignKey.getForeignTable(),
              foreignColumnSize });
          errors.add(new ForeignKeyError(columnName, label,
              ForeignKeyError.ERROR_SIZE, foreignColumn.getDataSize()));
        }
      }
    }
    return (ForeignKeyError[]) errors
        .toArray(new ForeignKeyError[errors.size()]);
  }

}
