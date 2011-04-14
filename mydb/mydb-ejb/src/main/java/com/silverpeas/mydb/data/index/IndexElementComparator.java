/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.mydb.data.index;

import java.util.Comparator;

/**
 * IndexElement comparator. Elements are sorted by their names if they are different, else sorted by
 * their position index.
 * @author Antoine HEDIN
 */
public class IndexElementComparator implements Comparator<IndexElement> {

  public int compare(IndexElement element1, IndexElement element2) {
    if (element1.getIndexName().equals(element2.getIndexName())) {
      return element1.getPosition() - element2.getPosition();
    } else {
      return element1.getIndexName().compareTo(element2.getIndexName());
    }
  }

}
