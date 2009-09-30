package com.silverpeas.mydb.data.db;

import java.util.Comparator;

/**
 * Database column comparator. Columns are sorted by their names.
 * 
 * @author Antoine HEDIN
 */
public class DbColumnComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    return ((DbColumn) o1).getName().compareTo(((DbColumn) o2).getName());
  }

}
