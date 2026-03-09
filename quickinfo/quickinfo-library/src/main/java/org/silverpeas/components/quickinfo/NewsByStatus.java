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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.service.QuickInfoDateComparatorDesc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.silverpeas.core.date.TemporalConverter.asDate;

public class NewsByStatus {

  List<News> visibles = new ArrayList<>();
  List<News> drafts = new ArrayList<>();
  List<News> notYetVisibles = new ArrayList<>();
  List<News> noMoreVisibles = new ArrayList<>();

  public NewsByStatus(List<News> allNews, String userId, NewsSort noMoreVisiblesSort, NewsSort notYetVisiblesSort) {
    for (News news : allNews) {
      if (news.isDraft()) {
        if (news.getCreatorId().equals(userId)) {
          drafts.add(news);
        }
      } else if (news.isVisible()) {
        visibles.add(news);
      } else if (news.isNoMoreVisible()) {
        noMoreVisibles.add(news);
      } else {
        notYetVisibles.add(news);
      }
    }
    sortByDateDesc(drafts);
    sortByDateDesc(visibles);
    notYetVisibles.sort(new QuickInfoBeginDateComparator(notYetVisiblesSort));
    noMoreVisibles.sort(new QuickInfoEndDateComparator(noMoreVisiblesSort));
  }

  private void sortByDateDesc(List<News> listOfNews) {
    Comparator<News> comparator = QuickInfoDateComparatorDesc.comparator;
    listOfNews.sort(comparator);
  }

  public List<News> getVisibles() {
    return visibles;
  }

  public List<News> getDrafts() {
    return drafts;
  }

  public List<News> getNotYetVisibles() {
    return notYetVisibles;
  }

  public List<News> getNoMoreVisibles() {
    return noMoreVisibles;
  }

  class QuickInfoBeginDateComparator implements Comparator<News> {

    private NewsSort sort;
    public QuickInfoBeginDateComparator(NewsSort sort) {
      this.sort = sort;
    }

    public int compare(News pd1, News pd2) {
      int result = asDate(pd1.getVisibility().getPeriod().getStartDate())
              .compareTo(asDate(pd2.getVisibility().getPeriod().getStartDate()));

      return sort == NewsSort.ASC ? result : -result;
    }
  }

  class QuickInfoEndDateComparator implements Comparator<News> {

    private NewsSort sort;
    public QuickInfoEndDateComparator(NewsSort sort) {
      this.sort = sort;
    }

    public int compare(News pd1, News pd2) {
      int result = asDate(pd1.getVisibility().getPeriod().getEndDate())
              .compareTo(asDate(pd2.getVisibility().getPeriod().getEndDate()));

      return sort == NewsSort.ASC ? result : -result;
    }
  }

}