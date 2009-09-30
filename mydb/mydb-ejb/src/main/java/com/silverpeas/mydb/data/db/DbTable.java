package com.silverpeas.mydb.data.db;

import java.util.ArrayList;
import java.util.Hashtable;

import com.silverpeas.mydb.data.key.PrimaryKey;

/**
 * Database table.
 * 
 * @author Antoine HEDIN
 */
public class DbTable {

  private String name;
  private PrimaryKey primaryKey;
  private ArrayList columns;
  private ArrayList lines;
  private int lineIndex;

  private Hashtable columnsIndexes;

  public DbTable(String name) {
    this.name = name;
    columns = new ArrayList();
    columnsIndexes = new Hashtable();
    lines = new ArrayList();
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
    return (DbColumn) columns.get(index);
  }

  public DbColumn getColumn(String name) {
    return (DbColumn) columns.get(getColumnIndex(name));
  }

  public int getColumnIndex(String name) {
    return ((Integer) columnsIndexes.get(name)).intValue();
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
      result[i] = ((DbColumn) columns.get(i)).getName();
    }
    return result;
  }

  public String[] getColumnsNames(boolean autoIncrementFilter) {
    if (autoIncrementFilter) {
      ArrayList columnsNamesList = new ArrayList();
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
    ArrayList resultList = new ArrayList();
    DbColumn column;
    for (int i = 0, n = columns.size(); i < n; i++) {
      column = (DbColumn) columns.get(i);
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
    ArrayList columnsNames = new ArrayList();
    DbColumn column;
    DbForeignKey foreignKey;
    for (int i = 0, n = columns.size(); i < n; i++) {
      column = (DbColumn) columns.get(i);
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