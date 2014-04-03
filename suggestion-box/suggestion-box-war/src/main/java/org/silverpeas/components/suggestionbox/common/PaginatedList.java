/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.common;

import java.util.ArrayList;
import java.util.List;

/**
 * A list representing a paginated view of another list.
 * @author mmoquillon
 * @param <T> the type of the items in the list.
 */
public class PaginatedList<T> extends ArrayList<T> {

  private int maxSize = -1;

  /**
   * Constructs a paginated list from the specified other list. The maximum size of the actual list
   * is the size of the specified list.
   * @param list the list from which a paginated view is built.
   */
  public PaginatedList(List<T> list) {
    super(list);
    maxSize = list.size();
  }

  /**
   * Constructs a paginated list from the specified other list and whose the maximum size is the
   * specified one.
   * @param list the list from which a paginated view is built.
   * @param maxSize the maximum size of the actual list.
   */
  public PaginatedList(List<T> list, int maxSize) {
    super(list);
    this.maxSize = maxSize;
  }

  /**
   * Gets the maximum size of the list from which this one is just a paginated view.
   * @return the maximum size of the actual list.
   */
  public long maxSize() {
    return maxSize;
  }
}
