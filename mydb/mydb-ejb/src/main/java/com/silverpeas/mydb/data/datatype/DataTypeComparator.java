package com.silverpeas.mydb.data.datatype;

import java.util.Comparator;

/**
 * Data types comparator. Data types are sorted by their names.
 * 
 * @author Antoine HEDIN
 * 
 */
public class DataTypeComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    return ((DataType) o1).getName().compareTo(((DataType) o2).getName());
  }

}
