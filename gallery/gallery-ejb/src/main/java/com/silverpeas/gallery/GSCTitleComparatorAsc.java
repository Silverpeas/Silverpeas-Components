package com.silverpeas.gallery;

import java.util.Comparator;

import com.silverpeas.gallery.model.PhotoDetail;

public class GSCTitleComparatorAsc implements Comparator {
  static public GSCTitleComparatorAsc comparator = new GSCTitleComparatorAsc();

  public int compare(Object o1, Object o2) {
    PhotoDetail photo1 = (PhotoDetail) o1;
    PhotoDetail photo2 = (PhotoDetail) o2;

    int compareResult = photo1.getTitle().toLowerCase().compareTo(
        photo2.getTitle().toLowerCase());
    if (compareResult == 0) {
      // les 2 photos ont le même titre, comparer les dates
      compareResult = photo1.getCreationDate().compareTo(
          photo2.getCreationDate());
    }
    return compareResult;
  }

  public boolean equals(Object o) {
    return o == this;
  }
}
