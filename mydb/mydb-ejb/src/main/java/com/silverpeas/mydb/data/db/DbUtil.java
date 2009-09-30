package com.silverpeas.mydb.data.db;

import java.util.StringTokenizer;

/**
 * Database tool class.
 * 
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
