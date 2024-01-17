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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.rssaggregator.service;

import com.rometools.rome.feed.synd.SyndEntry;

import java.util.Comparator;
import java.util.Date;

/**
 * A comparator of syndication feeds by their date.
 * @author mmoquillon
 */
public class FeedComparator implements Comparator<SyndEntry> {

  private final boolean reverseOrder;

  public FeedComparator() {
    this(false);
  }

  public FeedComparator(boolean reverseOrder) {
    this.reverseOrder = reverseOrder;
  }

  public int compare(SyndEntry feedEntry1, SyndEntry feedEntry2) {
    int cmp = 0;
    Date entry1Date = feedEntry1.getUpdatedDate() == null ? feedEntry1.getPublishedDate():feedEntry1.getUpdatedDate();
    Date entry2Date = feedEntry2.getUpdatedDate() == null ? feedEntry2.getPublishedDate():feedEntry2.getUpdatedDate();
    if (entry1Date != null && entry2Date != null) {
      cmp = entry1Date.compareTo(entry2Date);
    }
    return this.reverseOrder ? -1 * cmp : cmp;
  }
}
  