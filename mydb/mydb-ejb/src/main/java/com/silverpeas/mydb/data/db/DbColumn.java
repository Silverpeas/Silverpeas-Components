/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.mydb.data.db;

import java.util.ArrayList;

import com.silverpeas.mydb.data.datatype.DataTypeList;

/**
 * Database column.
 * 
 * @author Antoine HEDIN
 */
public class DbColumn {

  public static final String COLUMN_NAME = "COLUMN_NAME";
  public static final String DATA_TYPE = "DATA_TYPE";
  public static final String COLUMN_SIZE = "COLUMN_SIZE";
  public static final String NULLABLE = "NULLABLE";
  public static final String COLUMN_DEF = "COLUMN_DEF";

  public static final String FK_NAME = "FK_NAME";
  public static final String FKTABLE_NAME = "FKTABLE_NAME";
  public static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
  public static final String PKTABLE_NAME = "PKTABLE_NAME";
  public static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";

  public static final int DEFAULT_DATA_TYPE = -999;
  public static final int DEFAULT_DATA_SIZE = -999;
  public static final int MAX_DATA_SIZE = 2147483647;

  private String name;
  private int dataType;
  private int dataSize;
  private boolean nullable;
  private boolean readOnly;
  private boolean autoIncrement;
  private String defaultValue;
  private DbForeignKey importedForeignKey;
  private ArrayList<DbForeignKey> exportedForeignKeys;

  public DbColumn(String name, int dataType, int dataSize, boolean nullable,
      String defaultValue) {
    this.name = name;
    this.dataType = dataType;
    setDataSize(dataSize);
    this.nullable = nullable;
    this.readOnly = false;
    this.autoIncrement = false;
    this.defaultValue = defaultValue;
  }

  public DbColumn(String name, int dataType, int dataSize) {
    this(name, dataType, dataSize, true, null);
  }

  public DbColumn(String name) {
    this(name, DEFAULT_DATA_TYPE, DEFAULT_DATA_SIZE, true, null);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDataType() {
    return dataType;
  }

  public void setDataType(int dataType) {
    this.dataType = dataType;
  }

  public int getDataSize() {
    return dataSize;
  }

  public String getDataSizeAsString() {
    return (dataSize != DbColumn.DEFAULT_DATA_SIZE ? String.valueOf(dataSize)
        : "");
  }

  public void setDataSize(int dataSize) {
    this.dataSize = ((dataSize != MAX_DATA_SIZE && dataSize >= 0) ? dataSize
        : DEFAULT_DATA_SIZE);
  }

  public void removeDataSize() {
    this.dataSize = DEFAULT_DATA_SIZE;
  }

  public boolean hasDataSize() {
    return (dataSize != DEFAULT_DATA_SIZE);
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public String getDefaultValueAsString() {
    return (hasDefaultValue() ? defaultValue : "");
  }

  public boolean hasDefaultValue() {
    return (defaultValue != null && defaultValue.length() > 0);
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public DbForeignKey getImportedForeignKey() {
    return importedForeignKey;
  }

  public boolean hasImportedForeignKey() {
    return (importedForeignKey != null);
  }

  public void setImportedForeignKey(DbForeignKey importedForeignKey) {
    this.importedForeignKey = importedForeignKey;
  }

  public DbForeignKey[] getExportedForeignKeys() {
    return (exportedForeignKeys != null ? (DbForeignKey[]) exportedForeignKeys
        .toArray(new DbForeignKey[exportedForeignKeys.size()])
        : new DbForeignKey[0]);
  }

  public boolean hasExportedForeignKeys() {
    return (exportedForeignKeys != null);
  }

  public void addExportedForeignKey(DbForeignKey exportedForeignKey) {
    if (exportedForeignKeys == null) {
      exportedForeignKeys = new ArrayList<DbForeignKey>();
    }
    exportedForeignKeys.add(exportedForeignKey);
  }

  public void update(DbColumn column) {
    this.name = column.getName();
    this.dataType = column.getDataType();
    this.dataSize = column.getDataSize();
    this.nullable = column.isNullable();
    this.defaultValue = column.getDefaultValue();
  }

  public String getInfo(DataTypeList dataTypeList) {
    StringBuffer info = new StringBuffer(50);
    String columnName = getName();
    if (columnName != null && columnName.length() > 0) {
      info.append(columnName);
      String dataType = dataTypeList.getDataTypeName(getDataType());
      if (dataType != null && dataType.length() > 0) {
        info.append(" - ").append(dataType);
        if (hasDataSize()) {
          info.append("(").append(getDataSize()).append(")");
        }
      }
    }
    return info.toString();
  }

}