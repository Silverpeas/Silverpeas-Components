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

import java.util.StringTokenizer;

/**
 * Database tool class.
 * @author Antoine HEDIN
 */
public class DbUtil {

  public static String KEY_SEPARATOR = "#";
  public static String STRING_SEPARATOR = ", ";

  public static String getListAsKey(String[] values) {
    return getList(values, KEY_SEPARATOR);
  }

  public static String getListAsString(String[] values) {
    return getList(values, STRING_SEPARATOR);
  }

  private static String getList(String[] values, String separator) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0, n = values.length; i < n; i++) {
      if (i > 0) {
        sb.append(separator);
      }
      sb.append(values[i]);
    }
    return sb.toString();
  }

  public static String[] getListFromKeys(String value) {
    StringTokenizer st = new StringTokenizer(value, KEY_SEPARATOR);
    String[] list = new String[st.countTokens()];
    int index = 0;
    while (st.hasMoreTokens()) {
      list[index] = st.nextToken();
      index++;
    }
    return list;
  }

}
