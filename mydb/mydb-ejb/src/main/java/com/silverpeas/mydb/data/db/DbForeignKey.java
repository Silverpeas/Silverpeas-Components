package com.silverpeas.mydb.data.db;

/**
 * Database foreign key.
 * 
 * @author Antoine HEDIN
 */
public class DbForeignKey {

  private String keyName;
  private String tableName;
  private String columnName;

  public DbForeignKey(String keyName, String tableName, String columnName) {
    this.keyName = keyName;
    this.tableName = tableName;
    this.columnName = columnName;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public String getKeyName() {
    return keyName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

}
