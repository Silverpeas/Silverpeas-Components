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
 * 
 * @author Antoine HEDIN
 */
public class ForeignKeys {

  private ArrayList foreignKeys;
  private DbTable parentTable;

  public ForeignKeys(DbTable parentTable) {
    foreignKeys = new ArrayList();
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
    ArrayList list = new ArrayList();
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
      ArrayList uKeys = new ArrayList();
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
    ArrayList errors = new ArrayList();
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
