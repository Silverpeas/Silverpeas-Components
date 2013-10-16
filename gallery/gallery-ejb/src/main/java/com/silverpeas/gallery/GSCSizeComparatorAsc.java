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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.gallery;

import com.silverpeas.gallery.model.PhotoDetail;

import java.util.Comparator;

public class GSCSizeComparatorAsc implements Comparator<PhotoDetail> {
  static public GSCSizeComparatorAsc comparator = new GSCSizeComparatorAsc();

  /**
   * Compare photos on their sizes. if the sizes are identical, use the creation date.
   * @param photo1
   * @param photo2
   * @return
   */
  public int compare(PhotoDetail photo1, PhotoDetail photo2) {
    if (photo1.getImageSize() == photo2.getImageSize()) {
      return photo1.getCreationDate().compareTo(photo2.getCreationDate());
    }
    return Long.valueOf(photo1.getImageSize()).compareTo(photo2.getImageSize());
  }

  public boolean equals(Object o) {
    return o == this;
  }
}