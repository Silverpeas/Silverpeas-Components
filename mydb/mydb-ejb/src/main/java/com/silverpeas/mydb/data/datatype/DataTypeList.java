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

package com.silverpeas.mydb.data.datatype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * List of data types available for a specific database driver.
 * @author Antoine HEDIN.
 */
public class DataTypeList {

  private static final int DEFAULT_CAPACITY = 10;

  private Hashtable<String, DataType> dataTypeList;

  public DataTypeList() {
    dataTypeList = new Hashtable<String, DataType>(DEFAULT_CAPACITY);
  }

  public DataTypeList(int initialCapacity) {
    dataTypeList = new Hashtable<String, DataType>(initialCapacity);
  }

  public void add(DataType dataType) {
    dataTypeList.put(dataType.getName(), dataType);
  }

  public DataType get(String name) {
    return (DataType) dataTypeList.get(name);
  }

  public DataType get(int type) {
    DataType dataType;
    String name;
    for (Enumeration<String> en = dataTypeList.keys(); en.hasMoreElements();) {
      name = en.nextElement();
      dataType = get(name);
      if (dataType.getSqlType() == type) {
        return dataType;
      }
    }
    return null;
  }

  public boolean contains(int type) {
    return (get(type) != null);
  }

  public DataType[] getDataTypes() {
    ArrayList<DataType> list = new ArrayList<DataType>(dataTypeList.size());
    String name;
    for (Enumeration<String> en = dataTypeList.keys(); en.hasMoreElements();) {
      name = en.nextElement();
      list.add(get(name));
    }
    Collections.sort(list, new DataTypeComparator());
    return (DataType[]) list.toArray(new DataType[list.size()]);
  }

  public String getDataTypeName(int type) {
    DataType dataType = get(type);
    return (dataType != null ? dataType.getName() : "");
  }

  public boolean isSizeEnabled(int type) {
    DataType dataType = get(type);
    return (dataType == null || dataType.isSizeEnabled());
  }

}
