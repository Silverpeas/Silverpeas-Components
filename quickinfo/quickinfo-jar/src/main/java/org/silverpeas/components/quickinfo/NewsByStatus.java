/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.silverpeas.components.quickinfo.model.News;

import com.stratelia.webactiv.quickinfo.control.QuickInfoDateComparatorDesc;

public class NewsByStatus {
  
  List<News> visibles = new ArrayList<News>();
  List<News> drafts = new ArrayList<News>();
  List<News> notYetVisibles = new ArrayList<News>();
  List<News> noMoreVisibles = new ArrayList<News>();
  
  public NewsByStatus(List<News> allNews, String userId) {
    for (News news : allNews) {
      if (news.isDraft()) {
        if (news.getCreatedBy().equals(userId)) {
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
    Collections.sort(notYetVisibles, new QuickInfoBeginDateComparatorDesc());
    Collections.sort(noMoreVisibles, new QuickInfoEndDateComparatorDesc());
  }
  
  private void sortByDateDesc(List<News> listOfNews) {
    Comparator<News> comparator = QuickInfoDateComparatorDesc.comparator;
    Collections.sort(listOfNews, comparator);
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
  
  class QuickInfoBeginDateComparatorDesc implements Comparator<News> {
    public int compare(News pd1, News pd2) {
      return pd1.getVisibilityPeriod().getBeginDate().compareTo(pd2.getVisibilityPeriod().getBeginDate());
    }
  }
  
  class QuickInfoEndDateComparatorDesc implements Comparator<News> {
    public int compare(News pd1, News pd2) {
      return pd1.getVisibilityPeriod().getEndDate().compareTo(pd2.getVisibilityPeriod().getEndDate());
    }
  }
  
}