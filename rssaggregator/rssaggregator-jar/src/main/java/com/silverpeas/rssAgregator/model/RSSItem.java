/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.rssAgregator.model;

import de.nava.informa.core.ImageIF;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.Item;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

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
  private URL itemLink;
  /**
   * itemSubject the item subject
   */
  private String itemSubject;
  /**
   * itemDate the item date
   */
  private Date itemDate;
  /**
   * itemComments the item comments
   */
  private URL itemComments;

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
  private ImageIF channelImage;
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
   * @param channel
   */
  public RSSItem(Item item, Channel channel, SPChannel spChannel) {
    this.itemTitle = item.getTitle();
    this.itemDescription = item.getDescription();
    this.itemLink = item.getLink();
    this.itemSubject = item.getSubject();
    this.itemDate = item.getDate();
    this.itemComments = item.getComments();
    this.externalChannelId = channel.getId();
    this.channelTitle = channel.getTitle();
    this.channelImage = channel.getImage();
    this.channelDescription = channel.getDescription();
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
   * @param itemTitle the itemTitle to set
   */
  public void setItemTitle(String itemTitle) {
    this.itemTitle = itemTitle;
  }

  /**
   * @return the itemDescription
   */
  public String getItemDescription() {
    return itemDescription;
  }

  /**
   * @param itemDescription the itemDescription to set
   */
  public void setItemDescription(String itemDescription) {
    this.itemDescription = itemDescription;
  }

  /**
   * @return the itemURL
   */
  public URL getItemLink() {
    return itemLink;
  }

  /**
   * @param itemURL the itemURL to set
   */
  public void setItemLink(URL itemURL) {
    this.itemLink = itemURL;
  }

  /**
   * @return the itemSubject
   */
  public String getItemSubject() {
    return itemSubject;
  }

  /**
   * @param itemSubject the itemSubject to set
   */
  public void setItemSubject(String itemSubject) {
    this.itemSubject = itemSubject;
  }

  /**
   * @return the itemDate
   */
  public Date getItemDate() {
    return itemDate != null ? (Date) itemDate.clone() : null;
  }

  /**
   * @param itemDate the itemDate to set
   */
  public void setItemDate(Date itemDate) {
    this.itemDate = itemDate != null ? (Date) itemDate.clone() : null;
  }

  /**
   * @return the itemComments
   */
  public URL getItemComments() {
    return itemComments;
  }

  /**
   * @param itemComments the itemComments to set
   */
  public void setItemComments(URL itemComments) {
    this.itemComments = itemComments;
  }

  /**
   * @return the externalChannelId
   */
  public Long getExternalChannelId() {
    return externalChannelId;
  }

  /**
   * @param externalChannelId the externalChannelId to set
   */
  public void setExternalChannelId(Long externalChannelId) {
    this.externalChannelId = externalChannelId;
  }

  /**
   * @return the channelTitle
   */
  public String getChannelTitle() {
    return channelTitle;
  }

  /**
   * @param channelTitle the channelTitle to set
   */
  public void setChannelTitle(String channelTitle) {
    this.channelTitle = channelTitle;
  }

  /**
   * @return the channelDescription
   */
  public String getChannelDescription() {
    return channelDescription;
  }

  /**
   * @param channelDescription the channelDescription to set
   */
  public void setChannelDescription(String channelDescription) {
    this.channelDescription = channelDescription;
  }

  /**
   * @return the image
   */
  public ImageIF getChannelImage() {
    return channelImage;
  }

  /**
   * @param image the image to set
   */
  public void setChannelImage(ImageIF image) {
    this.channelImage = image;
  }

  /**
   * @return the channelId
   */
  public Long getChannelId() {
    return channelId;
  }

  /**
   * @param channelId the channelId to set
   */
  public void setChannelId(Long channelId) {
    this.channelId = channelId;
  }

  /**
   * @return the channelUrl
   */
  public String getChannelUrl() {
    return channelUrl;
  }

  /**
   * @param channelUrl the channelUrl to set
   */
  public void setChannelUrl(String channelUrl) {
    this.channelUrl = channelUrl;
  }

  /**
   * @return the nbDisplayedItems
   */
  public int getNbDisplayedItems() {
    return nbDisplayedItems;
  }

  /**
   * @param nbDisplayedItems the nbDisplayedItems to set
   */
  public void setNbDisplayedItems(int nbDisplayedItems) {
    this.nbDisplayedItems = nbDisplayedItems;
  }

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
}
