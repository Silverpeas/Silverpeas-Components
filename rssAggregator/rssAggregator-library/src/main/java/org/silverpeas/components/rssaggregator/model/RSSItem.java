/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.rssaggregator.model;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

/**
 * Use RSSItem to encapsulate RSS news or ATOM data. It allows us having light JSON transfer data.
 * Users who want more news about RSS only need to click on the items link.
 * @author ebonnet
 */
public class RSSItem implements Serializable, Comparable<RSSItem> {

  private static final long serialVersionUID = -1235557051682143463L;

  /*
   * Items attributes
   */
  /**
   * itemTitle the item title
   */
  private String itemTitle;
  /**
   * itemDescription the item description
   */
  private String itemDescription;
  /**
   * itemLink the item url link
   */
  private String itemLink;
  /**
   * itemDate the item date
   */
  private Date itemDate;
  /**
   * itemComments the item comments
   */
  private String itemComments;

  /*
   * Channel attributes
   */

  /**
   * channelId the channel identifier
   */
  private Long channelId;
  /**
   * externalChannelId the external channel identifier
   */
  private Long externalChannelId;
  /**
   * channelTitle the channel title
   */
  private String channelTitle;
  /**
   * channelDescription the channel description
   */
  private String channelDescription;
  /**
   * ImageIF the channel image
   */
  private SyndImage channelImage;
  /**
   * url the current channel URL that was filled by user
   */
  private String channelUrl;
  /**
   * nbDisplayedItems the number of displayed items
   */
  private int nbDisplayedItems;

  /**
   * Default RSSItem constructor which encapsulate Item and Channel from informa API
   * @param item
   * @param feed
   */
  public RSSItem(SyndEntry item, SyndFeed feed, SPChannel spChannel) {
    this.itemTitle = item.getTitle();
    this.itemDescription = item.getDescription() != null ? item.getDescription().getValue() : null;
    this.itemLink = item.getLink() == null ? item.getUri() : item.getLink();
    this.itemDate = item.getUpdatedDate() == null ? item.getPublishedDate() : item.getUpdatedDate();
    this.itemComments = item.getComments();
    this.externalChannelId = IdGenerator.GENERATOR.getId();
    this.channelTitle = feed.getTitle();
    this.channelImage = feed.getImage();
    this.channelDescription = feed.getDescription();
    this.channelId = Long.parseLong(spChannel.getPK().getId());
    this.channelUrl = spChannel.getUrl();
    this.nbDisplayedItems = spChannel.getNbDisplayedItems();
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
   * @return the itemComments
   */
  public String getItemComments() {
    return itemComments;
  }

  /**
   * @return the externalChannelId
   */
  public Long getExternalChannelId() {
    return externalChannelId;
  }

  /**
   * @return the channelTitle
   */
  public String getChannelTitle() {
    return channelTitle;
  }

  /**
   * @return the channelDescription
   */
  public String getChannelDescription() {
    return channelDescription;
  }

  /**
   * @return the image
   */
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
   * @return the channelUrl
   */
  public String getChannelUrl() {
    return channelUrl;
  }

  /**
   * @return the nbDisplayedItems
   */
  public int getNbDisplayedItems() {
    return nbDisplayedItems;
  }

  /**
   * Compares this RSS item with the specified one by their respective date. So, this method
   * breaks the property <code>(x.compareTo(y)==0) == (x.equals(y))</code>
   * @param other the other RSS item.
   * @return the comparing between their date.
   */
  @Override
  public int compareTo(RSSItem other) {
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

    if (itemTitle != null ? !itemTitle.equals(rssItem.itemTitle) : rssItem.itemTitle != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(rssItem.channelId) : rssItem.channelId != null) {
      return false;
    }
    return compareTo(rssItem) == 0;

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(itemTitle).append(channelId).append(itemDate).toHashCode();
  }

  private static class IdGenerator {

    public static final IdGenerator GENERATOR = new IdGenerator();
    private static final long SEED = 100000L;
    private final Random rand;

    private IdGenerator() {
      rand = new Random(System.currentTimeMillis());
    }

    public long getId() {
      return SEED + rand.nextLong();
    }
  }
}
