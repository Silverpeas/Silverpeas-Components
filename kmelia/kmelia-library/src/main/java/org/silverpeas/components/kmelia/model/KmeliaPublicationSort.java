/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.model;

import java.io.Serializable;
import java.util.List;

public class KmeliaPublicationSort implements Serializable {
  private static final long serialVersionUID = 123918700477888284L;

  public static final int SORT_CREATOR_ASC = 0;
  public static final int SORT_UPDATE_ASC = 1;
  public static final int SORT_UPDATE_DESC = 2;
  public static final int SORT_IMPORTANCE_ASC = 3;
  public static final int SORT_TITLE_ASC = 4;
  public static final int SORT_CREATION_ASC = 5;
  public static final int SORT_CREATION_DESC = 6;
  public static final int SORT_DESCRIPTION_ASC = 7;
  public static final int SORT_MANUAL = 99;

  private int currentSort;
  private boolean explicitSort = false;
  private boolean manualSortEnable = false;

  public KmeliaPublicationSort(final int initialSort) {
    this.currentSort = initialSort;
  }

  public int getCurrentSort() {
    return currentSort;
  }

  public void setCurrentSort(final int currentSort) {
    this.currentSort = currentSort;
  }

  public boolean isExplicitSort() {
    return explicitSort;
  }

  public void setExplicitSort(final boolean explicitSort) {
    this.explicitSort = explicitSort;
  }

  public boolean isManualSortEnable() {
    return manualSortEnable;
  }

  public void setManualSortEnable(final boolean manualSortEnable) {
    this.manualSortEnable = manualSortEnable;
  }

  /**
   * Gets the publication consumer according to given content language.
   * @param language a content language (and not the user favorite one).
   * @return SortConsumer lambda to apply on {@link KmeliaPublication} list.
   */
  public SortConsumer<List<KmeliaPublication>> withContentLanguage(final String language) {
    return p -> {
      if (p != null) {
        switch (currentSort) {
          case SORT_CREATOR_ASC:
            p.sort(new PubliAuthorComparator(true));
            break;
          case SORT_UPDATE_ASC:
            p.sort(new PubliUpdateDateComparator(true));
            break;
          case SORT_UPDATE_DESC:
            p.sort(new PubliUpdateDateComparator(false));
            break;
          case SORT_IMPORTANCE_ASC:
            p.sort(new PubliImportanceComparator(false));
            break;
          case SORT_TITLE_ASC:
            p.sort(new PubliTitleComparator(true, language));
            break;
          case SORT_CREATION_ASC:
            p.sort(new PubliCreationDateComparator(true));
            break;
          case SORT_CREATION_DESC:
            p.sort(new PubliCreationDateComparator(false));
            break;
          case SORT_DESCRIPTION_ASC:
            p.sort(new PubliDescriptionComparator(true, language));
            break;
          default:
            // display publications according to manual order defined by admin
            p.sort(new PubliRankComparator(true));
        }
      }
    };
  }

  @FunctionalInterface
  public interface SortConsumer<T> {
    void sort(T t);
  }
}
