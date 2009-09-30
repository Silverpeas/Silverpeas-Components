package com.silverpeas.mydb.data.db;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Database record line.
 * 
 * @author Antoine HEDIN
 */
public class DbLine {

  private Hashtable dataList;

  public DbLine() {
    dataList = new Hashtable();
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
    for (Enumeration en = dataList.keys(); en.hasMoreElements();) {
      key = (String) en.nextElement();
      result[i][0] = key;
      result[i][1] = getData(key);
      i++;
    }
    return result;
  }

}
