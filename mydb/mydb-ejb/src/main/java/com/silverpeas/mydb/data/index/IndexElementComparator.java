package com.silverpeas.mydb.data.index;

import java.util.Comparator;

/**
 * IndexElement comparator. Elements are sorted by their names if they are
 * different, else sorted by their position index.
 * 
 * @author Antoine HEDIN
 */
public class IndexElementComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    IndexElement element1 = (IndexElement) o1;
    IndexElement element2 = (IndexElement) o2;
    if (element1.getIndexName().equals(element2.getIndexName())) {
      return element1.getPosition() - element2.getPosition();
    } else {
      return element1.getIndexName().compareTo(element2.getIndexName());
    }
  }

}
