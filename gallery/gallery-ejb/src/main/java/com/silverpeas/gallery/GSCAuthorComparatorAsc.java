package com.silverpeas.gallery;

import java.util.Comparator;

import com.silverpeas.gallery.model.PhotoDetail;

public class GSCAuthorComparatorAsc implements Comparator {
  static public GSCAuthorComparatorAsc comparator = new GSCAuthorComparatorAsc();

  public int compare(Object o1, Object o2) {
    PhotoDetail photo1 = (PhotoDetail) o1;
    PhotoDetail photo2 = (PhotoDetail) o2;

    if (photo1.getAuthor() == null)
      return 1;
    if (photo2.getAuthor() == null)
      return -1;

    int compareResult = photo1.getAuthor().compareTo(photo2.getAuthor());
    if (compareResult == 0) {
      // les 2 photos on le même auteur, comparer les dates
      compareResult = photo1.getCreationDate().compareTo(
          photo2.getCreationDate());
    }
    return compareResult;
  }

  public boolean equals(Object o) {
    return o == this;
  }
}
