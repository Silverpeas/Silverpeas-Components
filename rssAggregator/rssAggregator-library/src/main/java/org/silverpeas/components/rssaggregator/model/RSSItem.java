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
package org.silverpeas.components.rssaggregator.model;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.kernel.annotation.NonNull;

import java.util.Date;
import java.util.Objects;

/**
 * Use RSSItem to encapsulate RSS news or ATOM data. It allows us having light JSON transfer data.
 * Users who want more news about RSS only need to click on the items link.
 * @author ebonnet
 */
public class RSSItem implements Comparable<RSSItem> {
  
  /*
   * Items attributes
   */
  /**
   * The item title
   */
  private final String itemTitle;
  /**
   * The item description
   */
  private final String itemDescription;
  /**
   * The item url link
   */
  private final String itemLink;
  /**
   * The item date
   */
  private final Date itemDate;

  /*
   * Channel attributes
   */

  /**
   * The channel identifier
   */
  private final Long channelId;
  /**
   * The channel title
   */
  private final String channelTitle;
  /**
   * The channel image
   */
  private final SyndImage channelImage;

  /**
   * Default RSSItem constructor which encapsulate Item and Channel from ROME API
   * @param item a RSS item from ROME API
   * @param feed the feed from which the item comes from.
   * @param spChannel the Silverpeas RSS channel with which the feed is mapped.
   */
  public RSSItem(SyndEntry item, SyndFeed feed, SPChannel spChannel) {
    this.itemTitle = item.getTitle();
    this.itemDescription = item.getDescription() != null ? item.getDescription().getValue() : null;
    this.itemLink = item.getLink() == null ? item.getUri() : item.getLink();
    this.itemDate = item.getUpdatedDate() == null ? item.getPublishedDate() : item.getUpdatedDate();
    this.channelTitle = feed.getTitle();
    this.channelImage = feed.getImage();
    this.channelId = Long.parseLong(spChannel.getPK().getId());
  }

  /**
   * @return the itemTitle
   */
  public String getItemTitle() {
    return itemTitle;
  }

  /**
   * @return the itemDescription
   */
  public String getItemDescription() {
    return itemDescription;
  }

  /**
   * @return the itemURL
   */
  public String getItemLink() {
    return itemLink;
  }

  /**
   * @return the itemDate
   */
  public Date getItemDate() {
    return itemDate != null ? (Date) itemDate.clone() : null;
  }

  /**
   * @return the channelTitle
   */
  public String getChannelTitle() {
    return channelTitle;
  }

  /**
   * @return the image
   */
  @SuppressWarnings("unused")
  public SyndImage getChannelImage() {
    return channelImage;
  }

  /**
   * @return the channelId
   */
  public Long getChannelId() {
    return channelId;
  }

  /**
   * Compares this RSS item with the specified one by their respective date. So, this method
   * breaks the property <code>(x.compareTo(y)==0) == (x.equals(y))</code>
   * @param other the other RSS item.
   * @return the comparing between their date.
   */
  @Override
  public int compareTo(@NonNull RSSItem other) {
    if (this.getItemDate() != null && other.getItemDate() != null) {
      return other.getItemDate().compareTo(this.getItemDate());
    } else if (this.getItemDate() == null && other.getItemDate() != null) {
      return 1;
    } else if (this.getItemDate() != null && other.getItemDate() == null) {
      return -1;
    }
    return 0;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RSSItem)) {
      return false;
    }

    final RSSItem rssItem = (RSSItem) o;

    if (!Objects.equals(itemTitle, rssItem.itemTitle)) {
      return false;
    }
    if (!Objects.equals(channelId, rssItem.channelId)) {
      return false;
    }
    return compareTo(rssItem) == 0;

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(itemTitle).append(channelId).append(itemDate).toHashCode();
  }
}
