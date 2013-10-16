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

package com.silverpeas.mydb.data.db;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Database record line.
 * @author Antoine HEDIN
 */
public class DbLine {

  private Hashtable<String, String> dataList;

  public DbLine() {
    dataList = new Hashtable<String, String>();
  }

  public void addData(String columnName, String value) {
    dataList.put(columnName, (value == null ? "" : value));
  }

  public String getData(String columnName) {
    return (String) dataList.get(columnName);
  }

  public String[][] getAllData() {
    String[][] result = new String[dataList.size()][2];
    String key;
    int i = 0;
    for (Enumeration<String> en = dataList.keys(); en.hasMoreElements();) {
      key = en.nextElement();
      result[i][0] = key;
      result[i][1] = getData(key);
      i++;
    }
    return result;
  }

}
