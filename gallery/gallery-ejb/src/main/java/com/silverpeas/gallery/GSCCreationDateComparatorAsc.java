/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery;

import java.util.Comparator;

import com.silverpeas.gallery.model.PhotoDetail;

public class GSCCreationDateComparatorAsc implements Comparator {
  static public GSCCreationDateComparatorAsc comparator = new GSCCreationDateComparatorAsc();

  public int compare(Object o1, Object o2) {
    PhotoDetail photo1 = (PhotoDetail) o1;
    PhotoDetail photo2 = (PhotoDetail) o2;

    int compareResult = photo1.getCreationDate().compareTo(
        photo2.getCreationDate());
    if (compareResult == 0) {
      // les 2 photos on été créée à la même date, comparer les Id
      compareResult = photo1.getPhotoPK().getId().compareTo(
          photo2.getPhotoPK().getId());
    }
    return compareResult;
  }

  public boolean equals(Object o) {
    return o == this;
  }
}
