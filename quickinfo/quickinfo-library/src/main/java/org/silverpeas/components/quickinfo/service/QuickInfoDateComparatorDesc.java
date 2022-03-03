/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo.service;

import java.util.Comparator;

import org.silverpeas.components.quickinfo.model.News;

public class QuickInfoDateComparatorDesc implements Comparator<News> {
  public static final QuickInfoDateComparatorDesc comparator = new QuickInfoDateComparatorDesc();

  /**
   * This result is reversed as we want a descending sort.
   */
  public int compare(News pd1, News pd2) {

    int compareResult;
    if (pd1.getOnlineDate() == null || pd2.getOnlineDate() == null) {
      if (pd1.getOnlineDate() != null) {
        compareResult = 1;
      } else {
        compareResult = -1;
      }
    } else {
      compareResult = pd1.getOnlineDate().compareTo(pd2.getOnlineDate());
    }
    if (compareResult == 0) {
      compareResult = pd1.getUpdateDate().compareTo(pd2.getUpdateDate());
    }
    return 0 - compareResult;
  }

}